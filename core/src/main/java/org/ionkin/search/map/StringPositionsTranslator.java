package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.LightString;

import java.util.Arrays;

public class StringPositionsTranslator extends StringTranslator<IntBytesMap> {
    @Override
    public byte[] serialize(LightString key, IntBytesMap value) {
        byte[] ar2 = value.fastSerialization();
        ByteArray buf = new ByteArray(1 + key.length() + ar2.length);
        buf.add(key);
        buf.add(ar2);
        return buf.getAll();
    }

    @Override
    public IntBytesMap deserializeValue(byte[] packed) {
        byte[] map = Arrays.copyOfRange(packed, 1 + packed[0], packed.length);
        return new IntBytesMap(map);
    }
}
