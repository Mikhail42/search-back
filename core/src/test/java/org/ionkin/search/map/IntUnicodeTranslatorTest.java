package org.ionkin.search.map;

import junit.framework.TestCase;

public class IntUnicodeTranslatorTest extends TestCase {

    public void testSerialize() {
        // TODO
        CompactHashMap<Integer, String> map = new CompactHashMap<>(new IntUnicodeTranslatorOld());
        map.put(4, "Базовая статья");
        String art = map.get(4);
        assertEquals("Базовая статья", art);
    }
}