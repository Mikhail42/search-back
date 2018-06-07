package org.ionkin.search.map;

import org.ionkin.search.BytesRange;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntBytesTranslatorTest {

    @Test
    public void serializeDeserialize() {
        IntBytesMap map = new IntBytesMap();
        map.put(5, new BytesRange(new byte[]{1, 2, 10, 3, 6}));
        map.put(6, new BytesRange(new byte[]{121, -2, 10, 3, -6}));
        map.put(2, new BytesRange(new byte[]{-1, 2, 10, -3, 6, 32}));

        assertArrayEquals(new byte[]{121, -2, 10, 3, -6}, map.get(6).getCopy());
        assertArrayEquals(new byte[]{-1, 2, 10, -3, 6, 32}, map.get(2).getCopy());
    }
}