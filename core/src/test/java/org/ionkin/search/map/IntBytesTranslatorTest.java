package org.ionkin.search.map;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntBytesTranslatorTest {

    @Test
    public void serializeDeserialize() {
        CompactHashMap<Integer, byte[]> map = new CompactHashMap<>(new IntBytesTranslator());
        map.put(5, new byte[]{1, 2, 10, 3, 6});
        map.put(6, new byte[]{121, -2, 10, 3, -6});
        map.put(2, new byte[]{-1, 2, 10, -3, 6, 32});

        assertArrayEquals(new byte[]{121, -2, 10, 3, -6}, map.get(6));
        assertArrayEquals(new byte[]{-1, 2, 10, -3, 6, 32}, map.get(2));
    }
}