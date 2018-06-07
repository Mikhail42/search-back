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

    public static int[] compressS9WithoutMemory(int[] data) {
        diff(data);
        return Simple9.compress(data);
    }

    public static int[] decompressS9WithMaxCount(BytesRange range, int max) {
        int[] out = Simple9.uncompressWithMaxCount(range, max);
        sum(out);
        return out;
    }

    public static int[] decompressS9(BytesRange range) {
        return decompressS9WithMaxCount(range, Integer.MAX_VALUE);
    }

    public static int[] decompressS9(byte[] data, IntWrapper inPos, int size) {
        int[] out = new int[size];
        Simple9.uncompress(data, inPos, out, new IntWrapper(), size);
        sum(out);
        return out;
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
        return decompressVb(bytes, 0, bytes.length, Integer.MAX_VALUE);
    }

    public static int[] decompressVb(byte[] bytes, int from, int until, int take) {
        int[] uncomp = VariableByte.uncompress(bytes, from, until, take);
        sum(uncomp);
        return uncomp;
    }

    public static int[] decompressVb(BytesRange range) {
        return decompressVb(range.getAll(), range.getFrom(), range.getTo(), range.length());
    }

    public static int[] decompressVb(BytesRange range, int count) {
        return decompressVb(range.getAll(), range.getFrom(), range.getTo(), Math.min(range.length(), count));
    }
}
