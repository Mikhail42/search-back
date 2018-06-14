package org.ionkin.search;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteArrayTest {

    @Test
    public void create() {
        ByteArray ar = new ByteArray();
        assertEquals(ar.size(), 0);
        assertEquals(ar.getCopy().length, 0);
        ar = new ByteArray(5);
        assertEquals(ar.size(), 0);
        assertArrayEquals(ar.getAll(), new byte[]{0, 0, 0, 0, 0});
        ar = new ByteArray(new byte[]{2, 4, 10, 20});
        assertEquals(ar.size(), 4);
        assertArrayEquals(ar.getCopy(), new byte[]{2, 4, 10, 20});
        assertArrayEquals(ar.getAll(), new byte[]{2, 4, 10, 20});
    }

    @Test
    public void addByte() {
        ByteArray ar = new ByteArray();
        ar.add((byte) 4);
        assertEquals(ar.size(), 1);
        assertArrayEquals(ar.getCopy(), new byte[]{4});
        assertArrayEquals(ar.getAll(), new byte[]{4});
    }

    @Test
    public void addBytes() {
        ByteArray ar = new ByteArray();
        ar.add(new byte[]{4});
        assertEquals(ar.size(), 1);
        assertArrayEquals(ar.getCopy(), new byte[]{4});
        ar.add(new byte[]{4, 5});
        assertArrayEquals(ar.getCopy(), new byte[]{4, 4, 5});
        assertEquals(ar.size(), 3);
    }

    @Test
    public void addString() {
        ByteArray ar = new ByteArray();
        ar.add(new LightString(new byte[]{50, 52, 60, 61, 55}));
        assertEquals(ar.size(), 6);
        assertArrayEquals(ar.getCopy(), new byte[]{5, 50, 52, 60, 61, 55});

        ar = new ByteArray(new byte[] {2, 4});
        ar.add(new LightString(new byte[]{50, 52, 60, 61, 55}));
        assertEquals(ar.size(), 8);
        assertArrayEquals(ar.getCopy(), new byte[]{2, 4, 5, 50, 52, 60, 61, 55});
    }

    @Test
    public void addVb() {
        ByteArray ar = new ByteArray();
        ar.addVb(5);
        assertEquals(ar.size(), 1);
        assertArrayEquals(ar.getCopy(), new byte[]{(byte)(5 | 128)});
    }

    @Test
    public void addRange() {
        ByteArray ar = new ByteArray();
        BytesRange range = new BytesRange(new byte[]{20, 25, 10, 23, 35, 25}, 1, 3);
        ar.add(range);
        assertEquals(ar.size(), 2);
        assertArrayEquals(ar.getCopy(), new byte[]{25, 10});
        ar.add(range);
        assertEquals(ar.size(), 4);
        assertArrayEquals(ar.getCopy(), new byte[]{25, 10, 25, 10});
    }
}