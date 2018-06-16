package org.ionkin.search;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class VariableByteTest {

    @Test
    public void compressUncompress() {
        int[] ar = new int[]{1, 4, 9, 10, 100, 400, 256, 257, 127, 128, 257, 129, 10000, 10000000};
        byte[] comp = VariableByte.compress(ar);
        int[] uncomp = VariableByte.uncompress(comp, ar.length);
        assertTrue(Arrays.equals(ar, uncomp));
    }

    @Test
    public void uncompressFirst() {
        byte[] ar = new byte[]{5, 14, 35, 44};
        for (int i = 0; i < ar.length; i++) ar[i] |= 128;
        assertEquals(VariableByte.uncompressFirst(ar, 0), 5);
        assertEquals(VariableByte.uncompressFirst(ar, 1), 14);
        assertEquals(VariableByte.uncompressFirst(ar, 2), 35);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 1)), 14);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 2)), 35);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 1), 1), 35);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 2), 1), 44);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 0, 1)), 5);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 0, 1), 0), 5);
        assertEquals(VariableByte.uncompressFirst(new BytesRange(ar, 0, 4), 3), 44);
    }

    @Test
    public void compress() {
        byte[] comp = VariableByte.compress(128 + 13);
        int[] ar = VariableByte.uncompress(comp, 1);
        assertArrayEquals(new int[]{128 + 13}, ar);
        assertArrayEquals(new byte[]{13, (byte)(1 | 128)}, comp);
    }

    @Test
    public void uncompressFirst2() {
        byte[] ar = VariableByte.compress((1 << 14) + 35);
        assertArrayEquals(new byte[]{35, 0, (byte) (1 | 128)}, ar);
        assertEquals(VariableByte.uncompressFirst(ar, 0), (1 << 14) + 35);
    }

    @Test
    public void uncompressFirst3() {
        byte[] ar = VariableByte.compress((1 << 21) + 35);
        assertArrayEquals(new byte[]{35, 0, 0, (byte) (1 | 128)}, ar);
        assertEquals(VariableByte.uncompressFirst(ar, 0), (1 << 21) + 35);
    }

    @Test
    public void decompressSize() {
        assertEquals(VariableByte.decompressSize(new byte[]{35, 0, 0, (byte) (1 | 128)}, 0, 4), 1);
        assertEquals(VariableByte.decompressSize(new byte[]{-1, -2, -4, -5}, 0, 4), 4);
        assertEquals(VariableByte.decompressSize(new byte[]{-1, -2, -4, -5}, 2, 4), 2);
        assertEquals(VariableByte.decompressSize(new byte[]{-1, -2, -4, -5}, 1, 3), 2);
        assertEquals(VariableByte.decompressSize(new BytesRange(new byte[]{-1, -2, -4, -5}, 1, 3)), 2);
    }

    @Test
    public void getNextPos() {
        assertEquals(VariableByte.getNextPos(new byte[]{35, 0, 0, (byte) (1 | 128)}, 0), 4);
        assertEquals(VariableByte.getNextPos(new byte[]{35, 0, -1, (byte) (1 | 128)}, 0), 3);
        assertEquals(VariableByte.getNextPos(new byte[]{-1, 0, -1, (byte) (1 | 128)}, 0), 1);

        assertEquals(VariableByte.getNextPos(new byte[]{35, 0, 0, (byte) (1 | 128)}, 1), 4);
        assertEquals(VariableByte.getNextPos(new byte[]{35, 0, -1, (byte) (1 | 128)}, 2), 3);
        assertEquals(VariableByte.getNextPos(new byte[]{-1, 0, -1, (byte) (1 | 128)}, 3), 4);
    }
}