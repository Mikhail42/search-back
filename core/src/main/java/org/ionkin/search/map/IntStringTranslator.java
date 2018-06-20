package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.LightString;
import org.ionkin.search.VariableByte;

import java.io.Serializable;
import java.util.Arrays;

public class IntStringTranslator extends IntTranslator<LightString> implements Serializable {

    @Override
    public byte[] serialize(Integer key, LightString value) {
        ByteArray ar = new ByteArray(value.length() + 1 + VariableByte.compressedLength(key));
        ar.addVb(key);
        ar.add(value);
        return ar.getAll();
    }

    @Override
    public LightString deserializeValue(byte[] packed) {
        int pos = VariableByte.getNextPos(packed, 0);
        return new LightString(Arrays.copyOfRange(packed, packed[pos] + 1, packed.length));
    }
}
