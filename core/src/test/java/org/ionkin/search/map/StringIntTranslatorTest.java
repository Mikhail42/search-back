package org.ionkin.search.map;

import junit.framework.TestCase;
import org.ionkin.search.LightString;

public class StringIntTranslatorTest extends TestCase {

    public void test() {
        CompactHashMap<LightString, Integer> map = new CompactHashMap<>(new StringIntTranslator());
        map.put(new LightString("мама"), 1733);
        assertEquals(map.get(new LightString("мама")).intValue(), 1733);
    }
}