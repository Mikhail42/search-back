package org.ionkin.search.map;

import org.ionkin.search.IO;
import org.ionkin.search.LightString;

import java.io.Serializable;

public class StringIntTranslator extends StringTranslator<Integer> implements Serializable {

    private static final long serialVersionUID = -6698208469153608363L;

    @Override
    public byte[] serialize(LightString key, Integer value) {
        byte[] ar = new byte[key.length() + 1 + 4];
        ar[0] = (byte) (key.length() & 0x7F);
        System.arraycopy(key.getBytes(), 0, ar, 1, ar[0]);
        IO.putInt(ar, value, key.length() + 1);
        return ar;
    }

    @Override
    public Integer deserializeValue(byte[] packed) {
        return IO.readInt(packed, packed[0] + 1);
    }
}
