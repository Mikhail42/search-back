package org.ionkin.search;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CompressorTest {

    @Test
    public void compressDecompress() {
        int[] ar = new int[]{7, 15, 16, 20, 28, 29, 30, 31, 32, 35, 36, 37, 38, 39, 40, 41, 45, 47, 49, 50, 51, 52};
        byte[] compVb = Compressor.compressVbWithoutMemory(ar);
        Compressor.sum(ar);
        int[] decompVb = Compressor.decompressVb(compVb);
        assertArrayEquals(ar, decompVb);

        /*int[] compS9 = Compressor.compressVbWithoutMemory(ar);
        ByteBuffer buf = ByteBuffer.allocate(compS9.length * 4);
        for (int c : compS9) {
            buf.putInt(c);
        }
        Compressor.sum(ar);
        int[] decompS9 = Compressor.decompressS9(buf.array(), new IntWrapper(), ar.length);
        assertTrue(Arrays.equals(ar, decompS9));*/
    }
/*
    @Test
    public void compRange() {
        int[] poss = Compressor.decompressS9(new BytesRange(new byte[]{-128, 0, 104, 104}));
        assertArrayEquals(poss, new int[] {26728});
    }

    @Test
    public void compRangeWithFrom() {
        int[] poss = Compressor.decompressS9(new BytesRange(new byte[]{12, 23, -128, 0, 104, 104}, 2));
        assertArrayEquals(poss, new int[] {26728});
    }*/

    @Test
    public void compressDecompressVb2() {
        int[] ar = new int[]{1, 2, 3, 4, 6, 8, 13, 23, 27, 27 + 256, 27 + 511, 1000, 1128, 1129, 1129 + 127, 2000};
        byte[] comp = Compressor.compressVbWithoutMemory(ar);
        Compressor.sum(ar);
        int[] decomp = Compressor.decompressVb(comp);
        assertArrayEquals(ar, decomp);
    }
}