package org.ionkin.search;

import java.util.Arrays;

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

    public static byte[] compressVbWithMemory(int[] data) {
        int[] copy = Arrays.copyOf(data, data.length);
        diff(copy);
        return VariableByte.compress(copy);
    }

    public static byte[] compressVbWithoutMemory(int[] data) {
        diff(data);
        return VariableByte.compress(data);
    }

    public static int[] decompressVb(byte[] bytes) {
        return decompressVb(bytes, 0, Integer.MAX_VALUE);
    }

    public static int[] decompressVb(byte[] bytes, int p, int take) {
        int[] uncomp = VariableByte.uncompress(bytes, p, take);
        sum(uncomp);
        return uncomp;
    }
}
