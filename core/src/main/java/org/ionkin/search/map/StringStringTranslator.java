package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.util.Arrays;

public class StringStringTranslator extends StringTranslator<LightString> {

    @Override
    public byte[] serialize(LightString key, LightString value) {
        int size = key.length() + value.length() + 1;
        byte[] packed = new byte[size];
        packed[0] = (byte) key.length();
        System.arraycopy(key.getBytes(), 0, packed, 1, key.length());
        System.arraycopy(value.getBytes(), 0, packed, 1 + key.length(), value.length());
        return packed;
    }

    @Override
    public LightString deserializeValue(byte[] packed) {
        byte[] str = Arrays.copyOfRange(packed, 1 + packed[0], packed.length);
        return new LightString(str);
    }
}
