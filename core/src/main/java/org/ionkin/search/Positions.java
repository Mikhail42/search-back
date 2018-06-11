package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.IntBytesMap;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Positions {

    static final int JUMP = 100;
    private static final int JUMP_SQR = JUMP * JUMP;

    private final int indexLength;
    private final int[] jumpSqr;
    private final int[] jump;
    public final byte[] indexPositions;

    public Positions(IntBytesMap positions) {
        this.indexLength = positions.size();
        this.jumpSqr = new int[jumpSqrSize(indexLength)];
        this.jump = new int[jumpSize(indexLength)];

        int k = 0;
        int[] keysSet = Ints.toArray(positions.keySet());
        Arrays.sort(keysSet);

        ByteArray acc = new ByteArray(keysSet.length * 20);
        for (int key : keysSet) {
            if (++k % JUMP == 0) {
                jump[k / JUMP - 1] = acc.size();
                if (k % JUMP_SQR == 0) {
                    jumpSqr[k / JUMP_SQR - 1] = k / JUMP - 1;
                }
            }

            BytesRange v = positions.get(key);
            acc.add(VariableByte.compress(key)); // docId
            acc.add(VariableByte.compress(v.length())); // range length
            acc.add(v); // range
        }

        this.indexPositions = acc.getCopy();
    }

    public byte[] serialize() {
        byte[] size = VariableByte.compress(indexLength);
        int[] jumpSqrComp = (jumpSqr.length != 0) ? Compressor.compressS9WithoutMemory(jumpSqr) : new int[0];
        int[] jumpComp = (jump.length != 0) ? Compressor.compressS9WithoutMemory(jump) : new int[0];

        ByteBuffer buf = ByteBuffer.allocate(size.length + (jumpComp.length + jumpSqrComp.length) * 4 + indexPositions.length);
        buf.put(size);
        for (int jS : jumpSqrComp) buf.putInt(jS);
        for (int j : jumpComp) buf.putInt(j);
        buf.put(indexPositions);

        return buf.array();
    }

    public static Positions deserialize(byte[] packed, int from) {
        int indexLength = VariableByte.uncompressFirst(packed, from);

        IntWrapper pos = new IntWrapper(VariableByte.compressedLength(indexLength) + from);
        int[] jumpSqr = Compressor.decompressS9(packed, pos, jumpSqrSize(indexLength));
        int[] jump = Compressor.decompressS9(packed, pos, jumpSize(indexLength));
        byte[] iPos = Arrays.copyOfRange(packed, pos.get(), packed.length);

        return new Positions(indexLength, jumpSqr, jump, iPos);
    }

    public Positions(int size, int[] jumpSqr, int[] jump, byte[] indexPositions) {
        this.indexLength = size;
        this.jumpSqr = jumpSqr;
        this.jump = jump;
        this.indexPositions = indexPositions;
    }

    public BytesRange positions(int docId) {
        int jumpInd = getJumpIndByDocIdWithJumpSqr(docId);
        int startPos = getPosByDocIdWithJump(docId, jumpInd);
        int docPos = getPosByDocId(docId, startPos);
        return getRangeByPackedPos(docPos);
    }

    private int getDocId(int pos) {
        return VariableByte.uncompressFirst(indexPositions, pos);
    }

    private BytesRange getRangeByPackedPos(int pos) {
        // docId length positions
        int lengthPos = VariableByte.getNextPos(indexPositions, pos);
        int length = VariableByte.uncompressFirst(indexPositions, lengthPos);
        int rangePos = VariableByte.getNextPos(indexPositions, lengthPos);
        return new BytesRange(indexPositions, rangePos, rangePos + length);
    }

    private int getJumpIndByDocIdWithJumpSqr(int docId) {
        if (jumpSqr.length == 0 || getDocId(jump[jumpSqr[0]]) > docId) {
            return 0;
        }
        int i = 0;
        while (i < jumpSqr.length && getDocId(jump[jumpSqr[i++]]) < docId) ;
        if (i == jumpSqr.length) i--;
        return jumpSqr[i];
    }

    private int getPosByDocIdWithJump(int docId, int jumpInd) {
        if (jump.length == 0 || getDocId(jump[0]) > docId) {
            return 0;
        }
        int i = jumpInd;
        while (i < jump.length && getDocId(jump[i++]) < docId) ;
        if (i == jump.length) i--;
        return jump[i];
    }

    private int getPosByDocId(int docId, int startPos) {
        int pos = startPos;
        while (getDocId(pos) < docId) {
            // docId length positions
            int curDIcId = VariableByte.uncompressFirst(indexPositions, pos);
            int lengthPos = VariableByte.getNextPos(indexPositions, pos);
            int rangeLength = VariableByte.uncompressFirst(indexPositions, lengthPos);
            int rangePos = VariableByte.getNextPos(indexPositions, lengthPos);
            pos = rangePos + rangeLength;
        }
        return pos;
    }

    private static int jumpSize(int indexLength) {
        return indexLength / JUMP;
    }

    private static int jumpSqrSize(int indexLength) {
        return indexLength / JUMP_SQR;
    }
}
