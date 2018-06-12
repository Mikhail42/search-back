package org.ionkin.search;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void merge() {
        int[] ar1 = new int[]{1, 5, 10, 15, 100};
        int[] ar2 = new int[]{1, 6, 10, 35, 100, 3534};
        int[] m = Util.merge(ar1, ar2);
        assertArrayEquals(m, new int[]{1, 5, 6, 10, 15, 35, 100, 3534});
    }
}