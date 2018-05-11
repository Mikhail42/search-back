package org.ionkin.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Compressor {
    private static final Logger logger = LoggerFactory.getLogger(Compressor.class);
    private static final VariableByte INT_COMPRESSOR = new VariableByte();

    public static void diff(int[] ints) {
        for (int i = ints.length - 2; i >= 0; i--) {
            ints[i + 1] -= ints[i];
        }
    }

    public static void sum(int[] ints) {
        for (int i = 0; i <= ints.length - 2; i++) {
            ints[i + 1] += ints[i];
        }
    }

    public static byte[] diffInts(byte[] ar) {
        int[] ints = IO.readArrayInt(ar, 0, ar.length / 4);
        diff(ints);
        return IO.toBytes(ints);
    }

    public static byte[] compressVbWithoutMemory(int[] data) {
        diff(data);
        return INT_COMPRESSOR.compress(data);
    }

    public static int[] decompressVb(byte[] bytes) {
        int[] uncomp = INT_COMPRESSOR.uncompress(bytes);
        sum(uncomp);
        return uncomp;
    }

    public static byte[] sumInts(byte[] ar) {
        int[] ints = IO.readArrayInt(ar, 0, ar.length / 4);
        for (int i = 0; i < ints.length - 1; i++) {
            ints[i + 1] += ints[i];
        }
        return IO.toBytes(ints);
    }

    public static byte[] diffAndCompressInts(int[] ar) {
        compressVbWithoutMemory(ar);
        byte[] bytes = IO.toBytes(ar);
        return diffAndCompressInts(bytes);
    }

    public static byte[] diffAndCompressInts(byte[] ar) {
        byte[] diff = diffInts(ar);
        return compressInts(diff);
    }

    public static byte[] decompressAndSumInts(byte[] ar) {
        byte[] decomp = decompressInts(ar);
        return sumInts(decomp);
    }

    // TODO
    public static byte[] decompressAndSumInts(byte[] ar, int intsCount) {
        byte[] decomp = decompressInts(ar, intsCount);
        return sumInts(decomp);
    }

    public static byte[] compressInts(byte[] ar) {
        List<Byte> compressed = new ArrayList<>();

        int posAtAr = 0;
        for (int i = 0; i < ar.length / 8; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                b |= (ar[posAtAr + j] == 0) ? 0 : (1 << (7 - j));
            }

            compressed.add(b);
            for (int j = 0; j < 8; j++) {
                if (ar[posAtAr + j] != 0) {
                    compressed.add(ar[posAtAr + j]);
                }
            }
            posAtAr += 8;
        }

        if (ar.length / 8 * 8 != ar.length) {
            byte b = 0;
            b |= (ar[posAtAr + 0] == 0) ? 0 : 128;
            b |= (ar[posAtAr + 1] == 0) ? 0 : 64;
            b |= (ar[posAtAr + 2] == 0) ? 0 : 32;
            b |= (ar[posAtAr + 3] == 0) ? 0 : 16;
            compressed.add(b);
            for (int j = 0; j < 4; j++) {
                if (ar[posAtAr + j] != 0) {
                    compressed.add(ar[posAtAr + j]);
                }
            }
        }

        byte[] c = new byte[compressed.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = compressed.get(i);
        }
        return c;
    }

    public static byte[] decompressInts(byte[] ar) {
        return decompressInts(ar, Integer.MAX_VALUE);
    }

    public static byte[] decompressInts(byte[] ar, int intsCount) {
        List<Byte> decompressed = new ArrayList<>();
        int bytesCount = (intsCount > Integer.MAX_VALUE / 4) ? Integer.MAX_VALUE : intsCount * 4;
        int pos = 0;
        while (pos < ar.length && decompressed.size() < bytesCount) {
            byte b = ar[pos++];
            for (int i = 7; i >= 0; i--) {
                if ((b & (1 << i)) == 0) {
                    decompressed.add((byte) 0);
                } else {
                    decompressed.add(ar[pos++]);
                }
            }
        }

        boolean lastNonEmpty = false;
        for (int i = decompressed.size() - 1; i >= decompressed.size() - 4; i--) {
            lastNonEmpty |= (decompressed.get(i) != 0);
        }
        int size = decompressed.size() - (lastNonEmpty ? 0 : 4);
        byte[] dec = new byte[size];
        for (int i = 0; i < size; i++) {
            dec[i] = decompressed.get(i);
        }
        return dec;
    }

    public static int decompressedLength(byte[] ar, int pos) {
        int commonLength = 0;
        while (pos < ar.length) {
            byte b = ar[pos++];
            commonLength++;
            pos += (b >> 7) & 1;
            pos += (b >> 6) & 1;
            pos += (b >> 5) & 1;
            pos += (b >> 4) & 1;
            int pos0 = pos;
            pos += (b >> 3) & 1;
            pos += (b >> 2) & 1;
            pos += (b >> 1) & 1;
            pos += b & 1;
            if (pos0 != pos) commonLength++;
        }
        return commonLength;
    }

    public static int decompressedLength(byte[] ar) {
        return decompressedLength(ar, 0);
    }
}
