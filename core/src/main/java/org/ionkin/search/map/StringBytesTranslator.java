package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.io.Serializable;
import java.util.Arrays;

public class StringBytesTranslator extends StringTranslator<byte[]> implements Serializable {

    private static final long serialVersionUID = -3160918799095615084L;

    @Override
    public byte[] serialize(LightString key, byte[] value) {
        byte[] packed = new byte[1 + key.length() + value.length];
        packed[0] = (byte) (key.length() & 0x7f);
        System.arraycopy(key.getBytes(), 0, packed, 1, key.length());
        System.arraycopy(value, 0, packed, key.length() + 1, value.length);
        return packed;
    }

    @Override
    public byte[] deserializeValue(byte[] packed) {
        int length = packed[0];
        return Arrays.copyOfRange(packed, length + 1, packed.length);
    }
}
