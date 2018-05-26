package org.ionkin.search.map;

import org.ionkin.search.VariableByte;

import java.util.ArrayList;

public class IntBytesTranslator extends IntTranslator<byte[]> {

    @Override
    public byte[] serialize(Integer key, byte[] value) {
        ArrayList<Byte> keyComp = VariableByte.compress(key);
        byte[] res = new byte[value.length + keyComp.size()];
        for (int i = 0; i < keyComp.size(); i++) {
            res[i] = keyComp.get(i);
        }
        System.arraycopy(value, 0, res, keyComp.size(), value.length);
        return res;
    }

    @Override
    public byte[] deserializeValue(byte[] packed) {
        int nextPos = VariableByte.getNextPos(packed, 0);
        byte[] value = new byte[packed.length - nextPos];
        System.arraycopy(packed, nextPos, value, 0, value.length);
        return value;
    }
}
