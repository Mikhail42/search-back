package org.ionkin.search.map;

import org.ionkin.search.*;

import java.io.Serializable;

public class StringIntsTranslator extends StringTranslator<IntsRange> implements Serializable {

    @Override
    public byte[] serialize(LightString key, IntsRange value) {
        ByteArray packed = new ByteArray(1 + key.length() + value.length() * 4);
        packed.add(key);
        packed.addRange(value);

        return packed.getAll();
    }

    @Override
    public IntsRange deserializeValue(byte[] packed) {
        int size = (packed.length - packed[0] - 1) / 4;
        int[] res = IO.readArrayInt(packed, packed[0] + 1, size);
        return new IntsRange(res);
    }
}
