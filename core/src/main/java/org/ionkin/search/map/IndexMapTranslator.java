package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.Index;
import org.ionkin.search.LightString;

public class IndexMapTranslator extends StringTranslator<Index> {

    @Override
    public byte[] serialize(LightString key, Index value) {
        byte[] val = value.serialize();
        ByteArray res = new ByteArray(1 + key.length() + val.length);
        res.add(key);
        res.add(val);
        return res.getAll();
    }

    @Override
    public Index deserializeValue(byte[] packed) {
        return Index.deserialize(packed, packed[0] + 1);
    }
}
