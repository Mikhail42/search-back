package org.ionkin.search;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BytesRangeTest {

    BytesRange range;

    @Before
    public void create() {
        range = new BytesRange(new byte[]{20, 25, 10, 23, 35, 25}, 1, 3);
    }

    @Test
    public void length() {
        assertEquals(2, range.length());
    }

    @Test
    public void getCopy() {
        assertArrayEquals(new byte[] {25, 10}, range.getCopy());
    }

    @Test
    public void getFrom() {
        assertEquals(1, range.getFrom());
    }

    @Test
    public void getTo() {
        assertEquals(range.getTo(), 3);
    }

    @Test
    public void getAll() {
        assertArrayEquals(new byte[] {20, 25, 10, 23, 35, 25}, range.getAll());
    }
}