package org.ionkin.search;

import org.ionkin.search.VariableByte;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class VariableByteTest {

    @Test
    public void compressUncompress() {
        int[] ar = new int[] {1, 4, 9, 10, 100, 400};
        byte[] comp = VariableByte.compress(ar);
        int[] uncomp = VariableByte.uncompress(comp, 6);
        assertTrue(Arrays.equals(ar, uncomp));
    }
}