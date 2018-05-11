package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.util.Arrays;

abstract class StringTranslator<V> implements CompactMapTranslator<LightString, V> {
    @Override
    public boolean isKeyInstance(Object obj) {
        return obj instanceof LightString;
    }

    @Override
    public int getHash(LightString key) {
        return key.hashCode();
    }

    @Override
    public LightString deserializeKey(byte[] packed) {
        int length = packed[0];
        byte[] str = Arrays.copyOfRange(packed, 1, 1 + length);
        return new LightString(str);
    }
}
