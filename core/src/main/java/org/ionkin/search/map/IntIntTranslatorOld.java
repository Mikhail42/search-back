package org.ionkin.search.map;

import org.ionkin.search.IO;
import org.ionkin.search.VariableByte;

import java.io.Serializable;

@Deprecated
public class IntIntTranslatorOld extends IntTranslatorOld<Integer> implements Serializable {

    private static final long serialVersionUID = 2711367714314452904L;

    @Override
    public byte[] serialize(Integer key, Integer value) {
        byte[] bytes = new byte[8];
        IO.putInt(bytes, key, 0);
        IO.putInt(bytes, value, 4);
        return bytes;
    }

    @Override
    public Integer deserializeValue(byte[] packed) {
        return IO.readInt(packed, 4);
    }
}
