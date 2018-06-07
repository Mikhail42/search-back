package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.BytesRange;
import org.ionkin.search.VariableByte;

public class IntBytesTranslator extends IntTranslator<BytesRange> {

    @Override
    public byte[] serialize(Integer key, BytesRange value) {
        // key(int) value(positions)
        byte[] keyComp = VariableByte.compress(key);
        ByteArray res = new ByteArray(keyComp.length + value.length());
        res.add(keyComp);
        res.add(value);
        return res.getAll();
    }

    @Override
    public BytesRange deserializeValue(byte[] packed) {
        int nextPos = VariableByte.getNextPos(packed, 0);
        return new BytesRange(packed, nextPos);
    }
}
