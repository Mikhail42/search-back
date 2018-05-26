package org.ionkin.search.map;

import com.google.common.primitives.Bytes;
import org.ionkin.search.VariableByte;

import java.util.Arrays;

public class BytesBytesTranslator extends BytesTranslator<byte[]> {

    @Override
    public byte[] serialize(byte[] key, byte[] value) {
        byte[] length = Bytes.toArray(VariableByte.compress(key.length));
        int size = length.length + key.length + value.length;
        byte[] res = new byte[size];
        System.arraycopy(length, 0, res, 0, length.length);
        System.arraycopy(key, 0, res, length.length, key.length);
        System.arraycopy(value, 0, res, length.length + key.length, value.length);
        return res;
    }

    @Override
    public byte[] deserializeValue(byte[] packed) {
        int length = VariableByte.uncompressFirst(packed, 0);
        int pos = VariableByte.compressedLength(length);
        return Arrays.copyOfRange(packed, pos + length, packed.length);
    }
}
