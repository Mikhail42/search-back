package org.ionkin.search.map;

import org.ionkin.search.*;

import java.io.Serializable;

public class StringBytesTranslator extends StringTranslator<BytesRange> implements Serializable {

    private static final long serialVersionUID = -3160918799095615084L;

    @Override
    public byte[] serialize(LightString key, BytesRange value) {
        ByteArray packed = new ByteArray(1 + key.length() + value.length());
        packed.add(key);
        packed.add(value);

        return packed.getAll();
    }

    @Override
    public BytesRange deserializeValue(byte[] packed) {
        return new BytesRange(packed, packed[0] + 1, packed.length);
    }
}
