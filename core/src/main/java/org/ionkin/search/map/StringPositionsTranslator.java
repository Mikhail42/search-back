package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.LightString;

import java.util.Arrays;

public class StringPositionsTranslator extends StringTranslator<IntBytesMap> {
    @Override
    public byte[] serialize(LightString key, IntBytesMap value) {
        byte[] serializedValue = value.fastSerialization();
        ByteArray buf = new ByteArray(1 + key.length() + serializedValue.length);
        buf.add(key);
        buf.add(serializedValue);
        return buf.getAll();
    }

    @Override
    public IntBytesMap deserializeValue(byte[] packed) {
        byte[] map = Arrays.copyOfRange(packed, 1 + packed[0], packed.length);
        return new IntBytesMap(map);
    }
}
