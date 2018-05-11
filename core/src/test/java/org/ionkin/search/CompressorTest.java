package org.ionkin.search;

import junit.framework.TestCase;

import java.util.Arrays;

public class CompressorTest extends TestCase {

    public void testCompressDecompressInts() {
        int[] ar = new int[]{1, 2, 3, 4, 6, 8, 13, 23};
        byte[] bytes = IO.toBytes(ar);
        assertEquals(bytes.length, 8 * 4);
        byte[] diff = Compressor.diffInts(bytes);
        assertEquals(diff.length, 8 * 4);
        byte[] comp = Compressor.compressInts(diff);
        assertEquals(comp.length, 4 + 8);
        byte[] decomp = Compressor.decompressInts(comp);
        assertEquals(decomp.length, 8 * 4);
        byte[] sums = Compressor.sumInts(decomp);
        assertEquals(sums.length, 8 * 4);
        int[] res = IO.toInts(sums);
        assertTrue(Arrays.equals(ar, res));
    }

    public void testCompressDecompressInts2() {
        int[] ar = new int[]{1, 2, 3, 4, 6, 8, 13, 23, 27, 1000, 1001};
        byte[] bytes = IO.toBytes(ar);
        assertEquals(bytes.length, ar.length * 4);
        byte[] diff = Compressor.diffInts(bytes);
        assertEquals(diff.length, ar.length * 4);
        byte[] comp = Compressor.compressInts(diff);
        assertEquals(comp.length, ar.length/2 + (ar.length % 2) + ar.length + 1); // 1 for diff 1000-27
        byte[] decomp = Compressor.decompressInts(comp);
        assertEquals(decomp.length, ar.length * 4);
        byte[] sums = Compressor.sumInts(decomp);
        assertEquals(sums.length, ar.length * 4);
        int[] res = IO.toInts(sums);
        assertTrue(Arrays.equals(ar, res));
    }

    public void testBoth() {
        byte[] ar = new byte[1400];
        Arrays.fill(ar, (byte) 4);
        byte[] comp = Compressor.compressInts(ar);
        byte[] ar2 = Compressor.decompressInts(comp);
        assertTrue(Arrays.equals(ar, ar2));
    }
}