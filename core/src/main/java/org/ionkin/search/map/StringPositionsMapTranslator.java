package org.ionkin.search.map;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.ionkin.search.Compressor;
import org.ionkin.search.IO;
import org.ionkin.search.LightString;
import org.ionkin.search.VariableByte;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class StringPositionsMapTranslator extends StringTranslator<CompactHashMap<Integer, byte[]>> {

    @Override
    public byte[] serialize(LightString lightString, CompactHashMap<Integer, byte[]> value) {
        Set<Integer> keySet = value.keySet();
        int[] keyAr = Ints.toArray(keySet);
        Arrays.sort(keyAr);
        byte[][] vals = new byte[keyAr.length][];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = value.get(keyAr[i]);
        }
        Compressor.diff(keyAr);

        int size = 0;
        for (int i = 0; i < vals.length; i++) {
            size += vals[i].length;
            size += VariableByte.compressedLength(keyAr[i]);
            size += VariableByte.compressedLength(vals[i].length);
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        for (int i = 0; i < vals.length; i++) {
            ArrayList<Byte> ar = VariableByte.compress(keyAr[i]);
            VariableByte.addCompressed(ar, vals[i].length);
            byte[] keyAsBytes = Bytes.toArray(ar);
            byteBuffer.put(keyAsBytes);
            byteBuffer.put(vals[i]);
        }

        byte[] map = byteBuffer.array();
        byte[] res = new byte[lightString.length() + 1 + map.length];
        IO.putString(res, lightString, 0);
        System.arraycopy(map, 0, res, lightString.length() + 1, map.length);
        return res;
    }

    @Override
    public CompactHashMap<Integer, byte[]> deserializeValue(byte[] packed) {
        int strLength = packed[0];
        byte[] map = new byte[packed.length - 1 - strLength];
        System.arraycopy(packed, strLength + 1, map, 0, map.length);
        return uncompress(map);
    }

    private CompactHashMap<Integer, byte[]> uncompress(byte[] mapAsBytes) {
        CompactHashMap<Integer, byte[]> map = new CompactHashMap<>(new IntBytesTranslator());
        int pos = 0;
        int prevKey = 0;
        while (pos < mapAsBytes.length) {
            int key = VariableByte.uncompressFirst(mapAsBytes, pos);
            int keyLengthComp = VariableByte.compressedLength(key);
            pos += keyLengthComp;

            int valLength = VariableByte.uncompressFirst(mapAsBytes, pos);
            int valLengthComp = VariableByte.compressedLength(valLength);
            pos += valLengthComp;

            byte[] val = new byte[valLength];
            System.arraycopy(mapAsBytes, pos, val, 0, valLength);
            pos += valLength;

            key += prevKey;
            map.put(key, val);
            prevKey = key;
        }
        return map;
    }
}
