package org.ionkin.search.map;

import org.ionkin.search.Util;
import org.ionkin.search.VariableByte;

import java.util.Arrays;

public abstract class BytesTranslator<V> implements CompactMapTranslator<byte[], V> {
    @Override
    public boolean isKeyInstance(Object obj) {
        return obj instanceof byte[];
    }

    @Override
    public int getHash(byte[] key) {
        return Util.hashCode(key);
    }

    @Override
    public byte[] deserializeKey(byte[] packed) {
        int length = VariableByte.uncompressFirst(packed, 0);
        int pos = VariableByte.compressedLength(length);
        return Arrays.copyOfRange(packed, pos, pos + length);
    }
}