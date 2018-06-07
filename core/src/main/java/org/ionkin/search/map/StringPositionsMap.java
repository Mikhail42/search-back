package org.ionkin.search.map;

import org.ionkin.search.BytesRange;
import org.ionkin.search.LightString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringPositionsMap extends CompactHashMap<LightString, IntBytesMap> {

    public StringPositionsMap() {
        super(new StringPositionsTranslator());
    }

    public StringPositionsMap(byte[] mapAsBytes) {
        super(new StringPositionsTranslator(), mapAsBytes);
    }

    public StringPositionsMap(byte[] mapAsBytes, int from) {
        super(new StringPositionsTranslator(), mapAsBytes, from);
    }

    public StringPositionsMap(String filename) throws IOException {
        super(new StringPositionsTranslator(), filename);
    }

    public static StringPositionsMap linkClone(CompactHashMap<LightString, IntBytesMap> map) {
        StringPositionsMap clone = new StringPositionsMap();
        clone.table = map.table;
        clone.lengthBits = map.lengthBits;
        clone.size = map.size;
        clone.filled = map.filled;
        clone.version = map.version;
        return clone;
    }

    public static void joinAtFirst(StringPositionsMap spm1, StringPositionsMap spm2) {
        spm2.forEach((k, v) -> {
            IntBytesMap ib1 = spm1.get(k);
            if (ib1 != null) {
                ib1.putAll(v);
                spm1.put(k, ib1);
            } else {
                spm1.put(k, v);
            }
        });
    }

    public static StringPositionsMap join(LightString[] words, StringPositionsMap[] maps) {
        StringPositionsMap res = new StringPositionsMap();
        for (LightString word : words) {
            // join positions for one word
            Map<Integer, BytesRange> articleIdPositionsMap = joinByWord(maps, word);

            IntBytesMap compact = new IntBytesMap();
            compact.putAll(articleIdPositionsMap);

            articleIdPositionsMap = null;
            if (!compact.isEmpty()) {
                res.put(word, compact);
            }
        }
        return res;
    }

    public static Map<Integer, BytesRange> joinByWord(StringPositionsMap[] maps, LightString word) {
        Map<Integer, BytesRange> res = new HashMap<>();
        for (StringPositionsMap map : maps) {
            Map<Integer, BytesRange> loc = map.get(word);
            if (loc != null) {
                res.putAll(loc);
            }
        }
        return res;
    }
}
