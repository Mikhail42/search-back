package org.ionkin.search.map;

import org.ionkin.search.VariableByte;

abstract class IntTranslator<V> implements CompactMapTranslator<Integer, V> {
    @Override
    public boolean isKeyInstance(Object obj) {
        return obj instanceof Integer;
    }

    @Override
    public int getHash(Integer key) {
        return key.hashCode();
    }

    @Override
    public Integer deserializeKey(byte[] packed) {
        return VariableByte.uncompressFirst(packed, 0);
    }
}
