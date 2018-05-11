package org.ionkin.search.map;

import org.ionkin.search.LightString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringMapTranslatorTest {

    CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map =
            new CompactHashMap<>(new StringPositionsMapTranslator());
    {
        CompactHashMap<Integer, byte[]> v1 = new CompactHashMap<>(new IntBytesTranslator());
        // (1 + 1 + 5) * 4 + (1 + 1 + 4) = 34
        v1.put(0, new byte[] {1, 3, 10, 100, 123});
        v1.put(1, new byte[] {1, 3, 10, 100, 123});
        v1.put(2, new byte[] {1, 3, 10, 100, 123});
        v1.put(3, new byte[] {1, 3, 10, 100, 123});
        v1.put(10, new byte[] {7, 10, 110, 123});

        // 1 + 1 + 4 + 34 = 40
        LightString k1 = new LightString("word");
        map.put(k1, v1);
        // 1 + 1 + 5 + 34 = 41
        LightString k2 = new LightString("word2");
        map.put(k2, v1);
    }

    @Test
    public void serializeDeserializeIntBytes() {
        for (int i = 0; i < 10; i++) {
            byte[] ser = map.serialize();
            System.err.println(ser.length);
            assertEquals(map.sizeOfTableWithLength(), ser.length);
            map = CompactHashMap.deserialize(ser, new StringPositionsMapTranslator());
            map.forEach((k, v) -> System.err.println(k + ", " + v.sizeOfTableWithLength()));
        }
        int a = 5;
    }
}