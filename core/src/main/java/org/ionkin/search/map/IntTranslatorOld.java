package org.ionkin.search.map;

import org.ionkin.search.IO;

@Deprecated
abstract class IntTranslatorOld<V> implements CompactMapTranslator<Integer, V> {
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
        return IO.readInt(packed, 0);
    }
}
