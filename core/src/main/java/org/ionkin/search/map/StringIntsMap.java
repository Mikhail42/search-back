package org.ionkin.search.map;

import org.ionkin.search.IntsRange;
import org.ionkin.search.Compressor;
import org.ionkin.search.LightString;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StringIntsMap extends CompactHashMap<LightString, IntsRange> {

    public StringIntsMap() {
        super(new StringIntsTranslator());
    }

    public StringIntsMap(String filename) throws IOException {
        super(new StringIntsTranslator(), filename);
    }

    public static StringIntsMap join(LightString[] words, StringIntsMap[] maps) {
        StringIntsMap res = new StringIntsMap();
        for (LightString word : words) {
            // join index for one word
            int size = 0;
            List<int[]> list = new LinkedList<>();
            for (StringIntsMap map : maps) {
                IntsRange ar = map.get(word);
                if (ar != null) {
                    int[] src = Compressor.compressS9WithoutMemory(ar.getCopy());
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
                int[] compact = Compressor.compressS9WithoutMemory(buf);
                res.put(word, new IntsRange(compact, 0, compact.length));
            }
        }
        return res;
    }
}
