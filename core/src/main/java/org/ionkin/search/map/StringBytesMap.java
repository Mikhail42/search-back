package org.ionkin.search.map;

import org.ionkin.search.Compressor;
import org.ionkin.search.LightString;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StringBytesMap extends CompactHashMap<LightString, byte[]> {

    public StringBytesMap() {
        super(new StringBytesTranslator());
    }

    public StringBytesMap(String filename) throws IOException {
        super(new StringBytesTranslator(), filename);
    }

    public int[] get(LightString key, int take) {
        int index = probe(key);
        if (index < 0) return new int[0];
        return ((StringBytesTranslator) translator).deserializeValue(table[index], take);
    }

    public static StringBytesMap join(LightString[] words, CompactHashMap<LightString, byte[]>[] maps) {
        StringBytesMap res = new StringBytesMap();
        for (LightString word : words) {
            // join index for one word
            int size = 0;
            List<int[]> list = new LinkedList<>();
            for (CompactHashMap<LightString, byte[]> map : maps) {
                byte[] ar = map.get(word);
                if (ar != null) {
                    int[] src = Compressor.decompressVb(ar);
                    list.add(src);
                    size += src.length;
                }
            }
            int[] buf = new int[size];
            AtomicInteger ai = new AtomicInteger(0);
            list.forEach(ar -> {
                System.arraycopy(ar, 0, buf, ai.get(), ar.length);
                ai.addAndGet(ar.length);
            });

            if (size != 0) {
                byte[] compact = Compressor.compressVbWithoutMemory(buf);
                res.put(word, compact);
            }
        }
        return res;
    }
}
