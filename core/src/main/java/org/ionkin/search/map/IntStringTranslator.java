package org.ionkin.search.map;

import org.ionkin.search.IO;
import org.ionkin.search.LightString;

import java.io.Serializable;

public class IntStringTranslator extends IntTranslator<LightString>implements Serializable {

    private static final long serialVersionUID = 1717040153505674621L;

    @Override
    public byte[] serialize(Integer key, LightString value) {
        byte[] bytes = new byte[4 + 1 + value.length()];
        IO.putInt(bytes, key, 0);
        IO.putString(bytes, value, 4);
        return bytes;
    }

    @Override
    public LightString deserializeValue(byte[] packed) {
        return IO.readString(packed, 4);
    }
}
