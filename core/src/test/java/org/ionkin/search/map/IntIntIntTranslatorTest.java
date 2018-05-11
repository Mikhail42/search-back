package org.ionkin.search.map;

import javafx.util.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntIntIntTranslatorTest {

    @Test
    public void serializeDeserialize() {
        CompactHashMap<Integer, Pair<Integer, Integer>> map = new CompactHashMap<>(new IntIntIntTranslator());
        map.put(5, new Pair<>(1, 2));
        map.put(6, new Pair<>(121, 3));
        map.put(2, new Pair<>(1, 2));

        assertEquals(new Pair<>(1, 2), map.get(5));
        assertEquals(new Pair<>(1, 2), map.get(2));
        assertEquals(new Pair<>(121, 3), map.get(6));
    }
}