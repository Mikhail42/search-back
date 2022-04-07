package org.ionkin.search.map;

import org.ionkin.search.BytesRange;
import org.ionkin.search.LightString;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringBytesTranslatorTest {

    private final StringBytesTranslator translator = new StringBytesTranslator();

    @Test
    public void testBothSerialize() {
        LightString key0 = new LightString("мама");
        byte[] ar = translator.serialize(key0, new BytesRange(new byte[] {1, 5, 9, 3, 2}));
        LightString key = translator.deserializeKey(ar);
        BytesRange value = translator.deserializeValue(ar);
        assertEquals(key, key0);
        assertArrayEquals(value.getCopy(), new byte[]{1, 5, 9, 3, 2});
    }
}