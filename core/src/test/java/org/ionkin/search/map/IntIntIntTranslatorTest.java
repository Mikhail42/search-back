package org.ionkin.search.map;

import org.ionkin.search.model.IntIntPair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntIntIntTranslatorTest {

    @Test
    public void serializeDeserialize() {
        CompactHashMap<Integer, IntIntPair> map = new CompactHashMap<>(new IntIntIntTranslator());
        map.put(5, new IntIntPair(1, 2));
        map.put(6, new IntIntPair(121, 3));
        map.put(2, new IntIntPair(1, 2));

        assertEquals(new IntIntPair(1, 2), map.get(5));
        assertEquals(new IntIntPair(1, 2), map.get(2));
        assertEquals(new IntIntPair(121, 3), map.get(6));
    }
}