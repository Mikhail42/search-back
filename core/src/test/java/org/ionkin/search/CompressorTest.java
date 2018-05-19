package org.ionkin.search;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class CompressorTest {

    @Test
    public void compressDecompressInts() {
        int[] ar = new int[]{1, 2, 3, 4, 6, 8, 13, 23};
        byte[] comp = Compressor.compressVbWithoutMemory(ar);
        Compressor.sum(ar);
        int[] decomp = Compressor.decompressVb(comp);
        assertTrue(Arrays.equals(ar, decomp));
    }

    @Test
    public void compressDecompressInts2() {
        int[] ar = new int[]{1, 2, 3, 4, 6, 8, 13, 23, 27, 1000, 1001};
        byte[] comp = Compressor.compressVbWithoutMemory(ar);
        Compressor.sum(ar);
        int[] decomp = Compressor.decompressVb(comp);
        assertTrue(Arrays.equals(ar, decomp));
    }
}