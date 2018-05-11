package org.ionkin.search.map;

import org.ionkin.search.IO;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Deprecated
public class IntUnicodeTranslatorOld extends IntTranslatorOld<String> implements Serializable {

    private static final long serialVersionUID = 8155972669115830592L;

    @Override
    public byte[] serialize(Integer key, String value) {
        byte[] res = new byte[4 + value.length() * 2];
        IO.putInt(res, key, 0);
        byte[] strBytes = value.getBytes(StandardCharsets.UTF_16);

        System.arraycopy(strBytes, 2, res, 4, strBytes.length - 2);
        return res;
    }

    @Override
    public String deserializeValue(byte[] packed) {
        byte p3 = packed[3]; byte p2 = packed[2];
        packed[2] = -2;  packed[3] = -1;
        String res = new String(packed, 2, packed.length - 2, StandardCharsets.UTF_16);
        packed[2] = p2; packed[3] = p3;
        return res;
    }
}
