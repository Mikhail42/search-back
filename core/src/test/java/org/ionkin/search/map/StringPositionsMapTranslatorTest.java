package org.ionkin.search.map;

import org.ionkin.search.LightString;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class StringPositionsMapTranslatorTest {

    StringPositionsMapTranslator translator = new StringPositionsMapTranslator();

    LightString k1 = new LightString("word");
    CompactHashMap<Integer, byte[]> v1 = new CompactHashMap<>(new IntBytesTranslator());
    {
        // (1 + 1 + 5) * 4 + (1 + 1 + 4) = 34
        v1.put(0, new byte[]{1, 3, 10, 100, 123});
        v1.put(1, new byte[]{1, 3, 10, 100, 123});
        v1.put(2, new byte[]{1, 3, 10, 100, 123});
        v1.put(3, new byte[]{1, 3, 10, 100, 123});
        v1.put(10, new byte[]{7, 10, 110, 123});
    }

    @Test
    public void serialize() {
        byte[] ar = translator.serialize(k1, v1);
        assertEquals(1 + 4 + 34, ar.length);
        assertEquals(ar[0], 4);
        assertTrue(ar[5] < 0);
        assertTrue(ar[6] < 0);
        assertEquals(1, ar[7]);
        assertEquals(3, ar[8]);
        assertEquals(10, ar[9]);
    }

    @Test
    public void deserializeValue() {
        byte[] ar = translator.serialize(k1, v1);
        CompactHashMap<Integer, byte[]> v2 = translator.deserializeValue(ar);

        v1.forEach((k, v) -> {
            assertTrue(v2.containsKey(k));
            assertTrue(Arrays.equals(v, v2.get(k)));
        });
    }
}