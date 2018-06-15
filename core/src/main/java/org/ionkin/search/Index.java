package org.ionkin.search;

import java.nio.ByteBuffer;

public class Index {
    static final int JUMP = 50;
    private static final int JUMP_SQR = JUMP * JUMP;
    private static final int JUMP_POW4 = JUMP_SQR * JUMP_SQR;

    private final int indexLength;
    private final int[] jumpPow4;
    private final int[] jumpSqr;
    // jump должен хранить не только ссылку на позицию, но и номер документа в этой позиции
    private final int[] jumpRef;
    private final int[] jumpSum;

    private final BytesRange index;

    private transient int currentPosition;
    private transient int currentSum;

    private Index(int indexLength, int[] jumpPow4, int[] jumpSqr, int[] jumpRef, int[] jumpSum, BytesRange index) {
        this.indexLength = indexLength;
        this.jumpPow4 = jumpPow4;
        this.jumpSqr = jumpSqr;
        this.jumpRef = jumpRef;
        this.jumpSum = jumpSum;
        this.index = index;
    }

    static Index fromOldIndex(int[] index) {
        return fromOldIndex(new BytesRange(Compressor.compressVbWithMemory(index)));
    }

    public static Index fromOldIndex(BytesRange index) {
        int[] ind = Compressor.decompressVb(index);
        int[] jumpPow4 = new int[jumpPow4Size(ind.length)];
        int[] jumpSqr = new int[jumpSqrSize(ind.length)];
        int[] jumpRef = new int[jumpSize(ind.length)];
        int[] jumpSum = new int[jumpSize(ind.length)];

        int pos = VariableByte.compressedLength(ind[0]);
        for (int i = 1; i < ind.length; i++) {
            if (i % JUMP == 0) {
                jumpRef[i / JUMP - 1] = pos;
                jumpSum[i / JUMP - 1] = ind[i - 1];
                if (i % JUMP_SQR == 0) {
                    jumpSqr[i / JUMP_SQR - 1] = i / JUMP - 1;
                    if (i % JUMP_POW4 == 0) {
                        jumpPow4[i / JUMP_POW4 - 1] = i / JUMP_SQR - 1;
                    }
                }
            }
            pos += VariableByte.compressedLength(ind[i] - ind[i - 1]);
        }

        return new Index(ind.length, jumpPow4, jumpSqr, jumpRef, jumpSum, index);
    }

    public static Index deserialize(byte[] packed, int from) {
        int indexLength = VariableByte.uncompressFirst(packed, from);
        IntWrapper pos = new IntWrapper(VariableByte.compressedLength(indexLength) + from);
        int[] jumpPow4 = Compressor.decompressS9(packed, pos, jumpPow4Size(indexLength));
        int[] jumpSqr = Compressor.decompressS9(packed, pos, jumpSqrSize(indexLength));
        int[] jumpRef = Compressor.decompressS9(packed, pos, jumpSize(indexLength));
        int[] jumpDocId = Compressor.decompressS9(packed, pos, jumpSize(indexLength));
        BytesRange index = new BytesRange(packed, pos.get(), packed.length);
        return new Index(indexLength, jumpPow4, jumpSqr, jumpRef, jumpDocId, index);
    }

    public byte[] serialize() {
        byte[] size = VariableByte.compress(indexLength);
        int[] jumpPow4Comp = (jumpPow4.length != 0) ? Compressor.compressS9WithoutMemory(jumpPow4) : new int[0];
        int[] jumpSqrComp = (jumpSqr.length != 0) ? Compressor.compressS9WithoutMemory(jumpSqr) : new int[0];
        int[] jumpRefComp = (jumpRef.length != 0) ? Compressor.compressS9WithoutMemory(jumpRef) : new int[0];
        int[] jumpDocIdComp = (jumpSum.length != 0) ? Compressor.compressS9WithoutMemory(jumpSum) : new int[0];
        int jumpCompByteSize = (jumpDocIdComp.length + jumpRefComp.length + jumpSqrComp.length + jumpPow4Comp.length) * 4;

        ByteBuffer buf = ByteBuffer.allocate(size.length + jumpCompByteSize + index.length());
        buf.put(size);
        for (int jP4 : jumpPow4Comp) buf.putInt(jP4);
        for (int jS : jumpSqrComp) buf.putInt(jS);
        for (int j : jumpRefComp) buf.putInt(j);
        for (int j : jumpDocIdComp) buf.putInt(j);
        buf.put(index.getCopy());

        return buf.array();
    }

    public BytesRange getIndexAsBytes() {
        return this.index;
    }

    public int[] getIndex(int take) {
        return Compressor.decompressVb(this.index, take);
    }

    public boolean containsDocWithGoToEffect(int docId) {
        int jumpSqrStartInd = getJumpSqrStartIndByDocIdWithJumpPow4(docId);
        int jumpStartInd = getJumpStartIndByDocIdWithJumpSqr(docId, jumpSqrStartInd);
        int jumpInd = getJumpIndByDocId(docId, jumpStartInd);
        this.currentPosition = jumpInd == -1 ? 0 : jumpRef[jumpInd];
        this.currentSum = jumpInd == -1 ? 0 : jumpSum[jumpInd];

        changeCurrentPosAndSum(docId);

        return currentPosition < index.length()
                && (getDocIdDiff(currentPosition) + currentSum) == docId;
    }

    public boolean hasNext() {
        return currentPosition < this.index.length();
    }

    public void goToStartPosition() {
        this.currentPosition = 0;
        this.currentSum = 0;
    }

    public int nextDocIdWithoutInc() {
        return currentSum + getDocIdDiff(currentPosition);
    }

    public int nextDocId() {
        int docIdDiff = getDocIdDiff(currentPosition);
        currentPosition += VariableByte.compressedLength(docIdDiff);
        currentSum += docIdDiff;
        return currentSum;
    }

    private int getDocIdDiff(int pos) {
        return VariableByte.uncompressFirst(index, pos);
    }

    private int getJumpSqrStartIndByDocIdWithJumpPow4(int docId) {
        // if pow4 jump is not need
        if (jumpPow4.length == 0) return 0;
        int i = 0;
        while (i < jumpPow4.length && (jumpSum[jumpSqr[jumpPow4[i]]] <= docId)) i++;
        if (i == jumpPow4.length) return jumpPow4[jumpPow4.length - 1];
        // else jumpSum[jumpSqr[jumpPow4[i]]] >= docId
        // and if i>= 0 then jumpSum[jumpSqr[jumpPow4[i-1]]] >= docId
        i--;
        return (i == -1) ? 0 : jumpPow4[i];
    }

    private int getJumpStartIndByDocIdWithJumpSqr(int docId, int jumpSqrStartInd) {
        // if sqr jump is not need
        if (jumpSqr.length == 0) return 0;
        int i = jumpSqrStartInd;
        while (i < jumpSqr.length && (jumpSum[jumpSqr[i]] <= docId)) i++;
        if (i == jumpSqr.length) return jumpSqr[jumpSqr.length - 1];
        i--;
        return (i == -1) ? 0 : jumpSqr[i];
    }

    private int getJumpIndByDocId(int docId, int jumpStartInd) {
        if (jumpRef.length == 0) return -1;
        int i = jumpStartInd;
        while (i < jumpSum.length && (jumpSum[i] <= docId)) i++;
        return i - 1;
    }

    private void changeCurrentPosAndSum(final int docId) {
        boolean flag = true;
        while (flag && currentPosition < index.length()) {
            int docIdDiff = VariableByte.uncompressFirst(index, currentPosition);
            flag = (docIdDiff + currentSum) < docId;
            if (flag) {
                currentSum += docIdDiff;
                currentPosition += VariableByte.compressedLength(docIdDiff);
            }
        }
    }

    private static int jumpSize(int indexLength) {
        return indexLength / JUMP;
    }

    private static int jumpSqrSize(int indexLength) {
        return indexLength / JUMP_SQR;
    }

    private static int jumpPow4Size(int indexLength) {
        return indexLength / JUMP_POW4;
    }

    public int getIndexLength() {
        return indexLength;
    }
}
