package org.ionkin.search;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class VariableByteTest {

    @Test
    public void compressUncompress() {
        int[] ar = new int[] {1, 4, 9, 10, 100, 400, 256, 257, 127, 128, 257, 129, 10000, 10000000};
        byte[] comp = VariableByte.compress(ar);
        int[] uncomp = VariableByte.uncompress(comp, ar.length);
        assertTrue(Arrays.equals(ar, uncomp));
    }
}