package org.ionkin.search.map;

import org.ionkin.search.LightString;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class StringMapTranslatorTest {

    StringPositionsMap map = new StringPositionsMap();
    LightString k1 = new LightString("word");
    LightString k2 = new LightString("word2");
    {
        IntBytesMap v1 = new IntBytesMap();
        v1.put(0, new byte[]{1, 3, 10, 100, 123});
        v1.put(1, new byte[]{1, 3, 10, 100, 123});
        v1.put(2, new byte[]{1, 3, 10, 100, 123});
        v1.put(3, new byte[]{1, 3, 10, 100, 123});
        v1.put(10, new byte[]{7, 10, 110, 123});

        map.put(k1, v1);
        map.put(k2, v1);
    }

    @Test
    public void serializeDeserializeIntBytes() {
        byte[] ser = map.fastSerialization();
        map = new StringPositionsMap(ser);
        assertTrue(Arrays.equals(map.get(k1).get(2), new byte[]{1, 3, 10, 100, 123}));
        assertTrue(Arrays.equals(map.get(k2).get(10), new byte[]{7, 10, 110, 123}));
    }
}