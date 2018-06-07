package org.ionkin.search.map;

import org.ionkin.search.ByteArray;
import org.ionkin.search.LightString;
import org.ionkin.search.Positions;

public class SearchTranslator extends StringTranslator<Positions> {

    @Override
    public byte[] serialize(LightString key, Positions value) {
        byte[] val = value.serialize();
        ByteArray buf = new ByteArray(1 + key.length() + val.length);
        buf.add(key);
        buf.add(val);
        return buf.getAll();
    }

    @Override
    public Positions deserializeValue(byte[] packed) {
        return Positions.deserialize(packed, packed[0] + 1);
    }
}
