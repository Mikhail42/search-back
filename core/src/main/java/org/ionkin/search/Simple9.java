package org.ionkin.search;

import java.util.Arrays;

/**
 * This is an implementation of the popular Simple9 scheme. It is limited to
 * 28-bit integers (between 0 and 2^28-1).
 * <p>
 * Note that this does not use differential coding: if you are working on sorted
 * lists, you must compute the deltas separately.
 *
 * @author Daniel Lemire
 * @editor Mikhail Ionkin
 */
public class Simple9 {

    private final static int bitLength[] = {1, 2, 3, 4, 5, 7, 9, 14, 28};

    private final static int codeNum[] = {28, 14, 9, 7, 5, 4, 3, 2, 1};

    public static void compress(int[] in, IntWrapper inPos, int inLength, int out[], IntWrapper outPos) {
        int tmpOutPos = outPos.get();
        int currentPos = inPos.get();
        final int finalIn = currentPos + inLength;
        outer:
        while (currentPos < finalIn - 28) { // write first 4 bits for each block
            mainloop:
            for (int selector = 0; selector < 8; selector++) {

                int res = 0;
                int compressedNum = codeNum[selector];
                int b = bitLength[selector];
                int max = 1 << b;
                int i = 0;
                for (; i < compressedNum; i++) {
                    if (max <= in[currentPos + i])
                        continue mainloop;
                    res = (res << b) + in[currentPos + i];
                }
                res |= selector << 28;
                out[tmpOutPos++] = res;
                currentPos += compressedNum;
                continue outer;
            }
            final int selector = 8;
            if (in[currentPos] >= 1 << bitLength[selector])
                throw new RuntimeException("Too big a number");
            out[tmpOutPos++] = in[currentPos++] | (selector << 28);
        }
        outer:
        while (currentPos < finalIn) {
            mainloop:
            for (int selector = 0; selector < 8; selector++) {
                int res = 0;
                int compressedNum = codeNum[selector];
                if (finalIn <= currentPos + compressedNum - 1)
                    compressedNum = finalIn - currentPos;
                int b = bitLength[selector];
                int max = 1 << b;
                int i = 0;
                for (; i < compressedNum; i++) {
                    if (max <= in[currentPos + i])
                        continue mainloop;
                    res = (res << b) + in[currentPos + i];
                }

                if (compressedNum != codeNum[selector])
                    res <<= (codeNum[selector] - compressedNum) * b;
                res |= selector << 28;
                out[tmpOutPos++] = res;
                currentPos += compressedNum;
                continue outer;
            }
            final int selector = 8;
            if (in[currentPos] >= 1 << bitLength[selector])
                throw new RuntimeException("Too big a number");
            out[tmpOutPos++] = in[currentPos++] | (selector << 28);
        }
        inPos.set(currentPos);
        outPos.set(tmpOutPos);
    }

    public static void uncompress(byte[] in, IntWrapper inpos, int[] out, IntWrapper outpos, int outlength) {
        int currentPos = outpos.get();
        int tmpInPos = inpos.get();
        final int finalout = currentPos + outlength;
        while (currentPos < finalout - 28) {
            int val = IO.readInt(in, tmpInPos);
            tmpInPos += 4;
            int header = val >>> 28;
            switch (header) {
                case 0: { // number : 28, bitwidth : 1
                    out[currentPos++] = (val << 4) >>> 31;
                    out[currentPos++] = (val << 5) >>> 31;
                    out[currentPos++] = (val << 6) >>> 31;
                    out[currentPos++] = (val << 7) >>> 31;
                    out[currentPos++] = (val << 8) >>> 31;
                    out[currentPos++] = (val << 9) >>> 31;
                    out[currentPos++] = (val << 10) >>> 31;
                    out[currentPos++] = (val << 11) >>> 31;
                    out[currentPos++] = (val << 12) >>> 31;
                    out[currentPos++] = (val << 13) >>> 31; // 10
                    out[currentPos++] = (val << 14) >>> 31;
                    out[currentPos++] = (val << 15) >>> 31;
                    out[currentPos++] = (val << 16) >>> 31;
                    out[currentPos++] = (val << 17) >>> 31;
                    out[currentPos++] = (val << 18) >>> 31;
                    out[currentPos++] = (val << 19) >>> 31;
                    out[currentPos++] = (val << 20) >>> 31;
                    out[currentPos++] = (val << 21) >>> 31;
                    out[currentPos++] = (val << 22) >>> 31;
                    out[currentPos++] = (val << 23) >>> 31; // 20
                    out[currentPos++] = (val << 24) >>> 31;
                    out[currentPos++] = (val << 25) >>> 31;
                    out[currentPos++] = (val << 26) >>> 31;
                    out[currentPos++] = (val << 27) >>> 31;
                    out[currentPos++] = (val << 28) >>> 31;
                    out[currentPos++] = (val << 29) >>> 31;
                    out[currentPos++] = (val << 30) >>> 31;
                    out[currentPos++] = (val << 31) >>> 31;
                    break;
                }
                case 1: { // number : 14, bitwidth : 2
                    out[currentPos++] = (val << 4) >>> 30;
                    out[currentPos++] = (val << 6) >>> 30;
                    out[currentPos++] = (val << 8) >>> 30;
                    out[currentPos++] = (val << 10) >>> 30;
                    out[currentPos++] = (val << 12) >>> 30;
                    out[currentPos++] = (val << 14) >>> 30;
                    out[currentPos++] = (val << 16) >>> 30;
                    out[currentPos++] = (val << 18) >>> 30;
                    out[currentPos++] = (val << 20) >>> 30;
                    out[currentPos++] = (val << 22) >>> 30; // 10
                    out[currentPos++] = (val << 24) >>> 30;
                    out[currentPos++] = (val << 26) >>> 30;
                    out[currentPos++] = (val << 28) >>> 30;
                    out[currentPos++] = (val << 30) >>> 30;
                    break;
                }
                case 2: { // number : 9, bitwidth : 3
                    out[currentPos++] = (val << 5) >>> 29;
                    out[currentPos++] = (val << 8) >>> 29;
                    out[currentPos++] = (val << 11) >>> 29;
                    out[currentPos++] = (val << 14) >>> 29;
                    out[currentPos++] = (val << 17) >>> 29;
                    out[currentPos++] = (val << 20) >>> 29;
                    out[currentPos++] = (val << 23) >>> 29;
                    out[currentPos++] = (val << 26) >>> 29;
                    out[currentPos++] = (val << 29) >>> 29;
                    break;
                }
                case 3: { // number : 7, bitwidth : 4
                    out[currentPos++] = (val << 4) >>> 28;
                    out[currentPos++] = (val << 8) >>> 28;
                    out[currentPos++] = (val << 12) >>> 28;
                    out[currentPos++] = (val << 16) >>> 28;
                    out[currentPos++] = (val << 20) >>> 28;
                    out[currentPos++] = (val << 24) >>> 28;
                    out[currentPos++] = (val << 28) >>> 28;
                    break;
                }
                case 4: { // number : 5, bitwidth : 5
                    out[currentPos++] = (val << 7) >>> 27;
                    out[currentPos++] = (val << 12) >>> 27;
                    out[currentPos++] = (val << 17) >>> 27;
                    out[currentPos++] = (val << 22) >>> 27;
                    out[currentPos++] = (val << 27) >>> 27;
                    break;
                }
                case 5: { // number : 4, bitwidth : 7
                    out[currentPos++] = (val << 4) >>> 25;
                    out[currentPos++] = (val << 11) >>> 25;
                    out[currentPos++] = (val << 18) >>> 25;
                    out[currentPos++] = (val << 25) >>> 25;
                    break;
                }
                case 6: { // number : 3, bitwidth : 9
                    out[currentPos++] = (val << 5) >>> 23;
                    out[currentPos++] = (val << 14) >>> 23;
                    out[currentPos++] = (val << 23) >>> 23;
                    break;
                }
                case 7: { // number : 2, bitwidth : 14
                    out[currentPos++] = (val << 4) >>> 18;
                    out[currentPos++] = (val << 18) >>> 18;
                    break;
                }
                case 8: { // number : 1, bitwidth : 28
                    out[currentPos++] = (val << 4) >>> 4;
                    break;
                }
                default: {
                    throw new RuntimeException("shouldn't happen: limited to 28-bit integers");
                }
            }
        }
        while (currentPos < finalout) {
            int val = IO.readInt(in, tmpInPos);
            tmpInPos += 4;
            int header = val >>> 28;
            switch (header) {
                case 0: { // number : 28, bitwidth : 1
                    final int howmany = finalout - currentPos;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (k + 4)) >>> 31;
                    }
                    break;
                }
                case 1: { // number : 14, bitwidth : 2
                    final int howmany = finalout - currentPos < 14 ? finalout - currentPos : 14;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (2 * k + 4)) >>> 30;
                    }
                    break;
                }
                case 2: { // number : 9, bitwidth : 3
                    final int howmany = finalout - currentPos < 9 ? finalout - currentPos : 9;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (3 * k + 5)) >>> 29;
                    }
                    break;
                }
                case 3: { // number : 7, bitwidth : 4
                    final int howmany = finalout - currentPos < 7 ? finalout - currentPos : 7;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (4 * k + 4)) >>> 28;
                    }
                    break;
                }
                case 4: { // number : 5, bitwidth : 5
                    final int howmany = finalout - currentPos < 5 ? finalout - currentPos : 5;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (5 * k + 7)) >>> 27;
                    }
                    break;
                }
                case 5: { // number : 4, bitwidth : 7
                    final int howmany = finalout - currentPos < 4 ? finalout - currentPos : 4;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (7 * k + 4)) >>> 25;
                    }
                    break;
                }
                case 6: { // number : 3, bitwidth : 9
                    final int howmany = finalout - currentPos < 3 ? finalout - currentPos : 3;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (9 * k + 5)) >>> 23;
                    }
                    break;
                }
                case 7: { // number : 2, bitwidth : 14
                    final int howmany = finalout - currentPos < 2 ? finalout - currentPos : 2;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (14 * k + 4)) >>> 18;
                    }
                    break;
                }
                case 8: { // number : 1, bitwidth : 28
                    out[currentPos++] = (val << 4) >>> 4;
                    break;
                }
                default: {
                    throw new RuntimeException("shouldn't happen");
                }
            }
        }
        outpos.set(currentPos);
        inpos.set(tmpInPos);
    }

    public static int[] uncompressWithMaxCount(BytesRange range, int max) {
        int[] out = new int[range.length() * 7];
        int currentPos = 0;
        int tmpInPos = range.getFrom();
        while (tmpInPos < range.getTo() - 4 && currentPos < max) {
            int val = IO.readInt(range.getAll(), tmpInPos);
            tmpInPos += 4;
            int header = val >>> 28;
            switch (header) {
                case 0: { // number : 28, bitwidth : 1
                    out[currentPos++] = (val << 4) >>> 31;
                    out[currentPos++] = (val << 5) >>> 31;
                    out[currentPos++] = (val << 6) >>> 31;
                    out[currentPos++] = (val << 7) >>> 31;
                    out[currentPos++] = (val << 8) >>> 31;
                    out[currentPos++] = (val << 9) >>> 31;
                    out[currentPos++] = (val << 10) >>> 31;
                    out[currentPos++] = (val << 11) >>> 31;
                    out[currentPos++] = (val << 12) >>> 31;
                    out[currentPos++] = (val << 13) >>> 31; // 10
                    out[currentPos++] = (val << 14) >>> 31;
                    out[currentPos++] = (val << 15) >>> 31;
                    out[currentPos++] = (val << 16) >>> 31;
                    out[currentPos++] = (val << 17) >>> 31;
                    out[currentPos++] = (val << 18) >>> 31;
                    out[currentPos++] = (val << 19) >>> 31;
                    out[currentPos++] = (val << 20) >>> 31;
                    out[currentPos++] = (val << 21) >>> 31;
                    out[currentPos++] = (val << 22) >>> 31;
                    out[currentPos++] = (val << 23) >>> 31; // 20
                    out[currentPos++] = (val << 24) >>> 31;
                    out[currentPos++] = (val << 25) >>> 31;
                    out[currentPos++] = (val << 26) >>> 31;
                    out[currentPos++] = (val << 27) >>> 31;
                    out[currentPos++] = (val << 28) >>> 31;
                    out[currentPos++] = (val << 29) >>> 31;
                    out[currentPos++] = (val << 30) >>> 31;
                    out[currentPos++] = (val << 31) >>> 31;
                    break;
                }
                case 1: { // number : 14, bitwidth : 2
                    out[currentPos++] = (val << 4) >>> 30;
                    out[currentPos++] = (val << 6) >>> 30;
                    out[currentPos++] = (val << 8) >>> 30;
                    out[currentPos++] = (val << 10) >>> 30;
                    out[currentPos++] = (val << 12) >>> 30;
                    out[currentPos++] = (val << 14) >>> 30;
                    out[currentPos++] = (val << 16) >>> 30;
                    out[currentPos++] = (val << 18) >>> 30;
                    out[currentPos++] = (val << 20) >>> 30;
                    out[currentPos++] = (val << 22) >>> 30; // 10
                    out[currentPos++] = (val << 24) >>> 30;
                    out[currentPos++] = (val << 26) >>> 30;
                    out[currentPos++] = (val << 28) >>> 30;
                    out[currentPos++] = (val << 30) >>> 30;
                    break;
                }
                case 2: { // number : 9, bitwidth : 3
                    out[currentPos++] = (val << 5) >>> 29;
                    out[currentPos++] = (val << 8) >>> 29;
                    out[currentPos++] = (val << 11) >>> 29;
                    out[currentPos++] = (val << 14) >>> 29;
                    out[currentPos++] = (val << 17) >>> 29;
                    out[currentPos++] = (val << 20) >>> 29;
                    out[currentPos++] = (val << 23) >>> 29;
                    out[currentPos++] = (val << 26) >>> 29;
                    out[currentPos++] = (val << 29) >>> 29;
                    break;
                }
                case 3: { // number : 7, bitwidth : 4
                    out[currentPos++] = (val << 4) >>> 28;
                    out[currentPos++] = (val << 8) >>> 28;
                    out[currentPos++] = (val << 12) >>> 28;
                    out[currentPos++] = (val << 16) >>> 28;
                    out[currentPos++] = (val << 20) >>> 28;
                    out[currentPos++] = (val << 24) >>> 28;
                    out[currentPos++] = (val << 28) >>> 28;
                    break;
                }
                case 4: { // number : 5, bitwidth : 5
                    out[currentPos++] = (val << 7) >>> 27;
                    out[currentPos++] = (val << 12) >>> 27;
                    out[currentPos++] = (val << 17) >>> 27;
                    out[currentPos++] = (val << 22) >>> 27;
                    out[currentPos++] = (val << 27) >>> 27;
                    break;
                }
                case 5: { // number : 4, bitwidth : 7
                    out[currentPos++] = (val << 4) >>> 25;
                    out[currentPos++] = (val << 11) >>> 25;
                    out[currentPos++] = (val << 18) >>> 25;
                    out[currentPos++] = (val << 25) >>> 25;
                    break;
                }
                case 6: { // number : 3, bitwidth : 9
                    out[currentPos++] = (val << 5) >>> 23;
                    out[currentPos++] = (val << 14) >>> 23;
                    out[currentPos++] = (val << 23) >>> 23;
                    break;
                }
                case 7: { // number : 2, bitwidth : 14
                    out[currentPos++] = (val << 4) >>> 18;
                    out[currentPos++] = (val << 18) >>> 18;
                    break;
                }
                case 8: { // number : 1, bitwidth : 28
                    out[currentPos++] = (val << 4) >>> 4;
                    break;
                }
                default: {
                    throw new RuntimeException("shouldn't happen: limited to 28-bit integers");
                }
            }
        }

        int endPos = range.getTo() - range.getFrom() - 1;
        while (range.get(endPos) == 0) endPos--;
        endPos++;
        while (tmpInPos < endPos + range.getFrom() && currentPos < max) {
            int val = IO.readInt(range.getAll(), tmpInPos);
            tmpInPos += 4;
            int header = val >>> 28;
            switch (header) {
                case 0: { // number : 28, bitwidth : 1
                    final int howmany = endPos - currentPos;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (k + 4)) >>> 31;
                    }
                    break;
                }
                case 1: { // number : 14, bitwidth : 2
                    final int howmany = endPos - currentPos < 14 ? endPos - currentPos : 14;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (2 * k + 4)) >>> 30;
                    }
                    break;
                }
                case 2: { // number : 9, bitwidth : 3
                    final int howmany = endPos - currentPos < 9 ? endPos - currentPos : 9;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (3 * k + 5)) >>> 29;
                    }
                    break;
                }
                case 3: { // number : 7, bitwidth : 4
                    final int howmany = endPos - currentPos < 7 ? endPos - currentPos : 7;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (4 * k + 4)) >>> 28;
                    }
                    break;
                }
                case 4: { // number : 5, bitwidth : 5
                    final int howmany = endPos - currentPos < 5 ? endPos - currentPos : 5;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (5 * k + 7)) >>> 27;
                    }
                    break;
                }
                case 5: { // number : 4, bitwidth : 7
                    final int howmany = endPos - currentPos < 4 ? endPos - currentPos : 4;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (7 * k + 4)) >>> 25;
                    }
                    break;
                }
                case 6: { // number : 3, bitwidth : 9
                    final int howmany = endPos - currentPos < 3 ? endPos - currentPos : 3;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (9 * k + 5)) >>> 23;
                    }
                    break;
                }
                case 7: { // number : 2, bitwidth : 14
                    final int howmany = endPos - currentPos < 2 ? endPos - currentPos : 2;
                    for (int k = 0; k < howmany; ++k) {
                        out[currentPos++] = (val << (14 * k + 4)) >>> 18;
                    }
                    break;
                }
                case 8: { // number : 1, bitwidth : 28
                    out[currentPos++] = (val << 4) >>> 4;
                    break;
                }
                default: {
                    throw new RuntimeException("shouldn't happen");
                }
            }
        }
        int ind = out.length - 1;
        while (out[ind] == 0) ind--;
        return Arrays.copyOfRange(out, 0, Math.min(ind + 1, max));
    }

    public static int[] compress(int[] in) {
        IntWrapper outPos = new IntWrapper();
        int[] out = new int[in.length];
        compress(in, new IntWrapper(), in.length, out, outPos);
        return Arrays.copyOf(out, outPos.get());
    }
}
