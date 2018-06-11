package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.VariableByte;

public class IntByteTranslator extends IntTranslator<Byte> {
    @Override
    public byte[] serialize(Integer key, Byte value) {
        ByteArray ar = new ByteArray();
        ar.addVb(key);
        ar.add(value);
        return ar.getCopy();
    }

    @Override
    public Byte deserializeValue(byte[] packed) {
        int pos = VariableByte.getNextPos(packed, 0);
        return packed[pos];
    }
}
