package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.LightString;
import org.ionkin.search.VariableByte;

import java.io.Serializable;

public class StringIntTranslator extends StringTranslator<Integer> implements Serializable {

    @Override
    public byte[] serialize(LightString key, Integer value) {
        ByteArray ar = new ByteArray(key.length() + 1 + VariableByte.compressedLength(value));
        ar.add(key);
        ar.addVb(value);
        return ar.getAll();
    }

    @Override
    public Integer deserializeValue(byte[] packed) {
        return VariableByte.uncompressFirst(packed, packed[0] + 1);
    }
}
