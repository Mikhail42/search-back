package org.ionkin.search;

public class Compressor {

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

    public static byte[] compressVbWithoutMemory(int[] data) {
        diff(data);
        return VariableByte.compress(data);
    }

    public static int[] decompressVb(byte[] bytes) {
        int[] uncomp = VariableByte.uncompress(bytes);
        sum(uncomp);
        return uncomp;
    }
}
