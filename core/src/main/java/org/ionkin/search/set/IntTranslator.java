package org.ionkin.search.set;

import org.ionkin.search.VariableByte;

public class IntTranslator implements CompactSetTranslator<Integer> {
    @Override
    public boolean isInstance(Object obj) {
        return obj instanceof Integer;
    }

    @Override
    public int getHash(Integer key) {
        return key.hashCode();
    }

    @Override
    public byte[] serialize(Integer obj) {
        return VariableByte.compress(obj);
    }

    @Override
    public Integer deserialize(byte[] packed) {
        return VariableByte.uncompressFirst(packed, 0);
    }
}
