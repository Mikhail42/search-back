package org.ionkin.search.map;

import junit.framework.TestCase;
import org.ionkin.search.LightString;

import java.util.Arrays;

public class StringBytesTranslatorTest extends TestCase {

    CompactMapTranslator<LightString, byte[]> translator = new StringBytesTranslator();

    public void testBothSerialize() {
        LightString key0 = new LightString("мама");
        byte[] ar = translator.serialize(key0, new byte[] {1, 5, 9, 3, 2});
        LightString key = translator.deserializeKey(ar);
        byte[] value = translator.deserializeValue(ar);
        assertEquals(key, key0);
        assertTrue(Arrays.equals(value, new byte[] {1, 5, 9, 3, 2}));
    }
}