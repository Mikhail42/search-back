package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringPositionsMap extends CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> {

    public StringPositionsMap() {
        super(new StringPositionsMapTranslator());
    }

    public StringPositionsMap(byte[] content) {
        super(new StringPositionsMapTranslator(), content);
    }

    public StringPositionsMap(String filename) throws IOException {
        super(new StringPositionsMapTranslator(), filename);
    }

    /*public BytesBytesMap asBytesBytesMap() {
        BytesBytesMap sbMap = new BytesBytesMap();
        StringPositionsMapTranslator translator = (StringPositionsMapTranslator) this.translator;
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                LightString key = translator.deserializeKey(table[i]);
                IntBytesMap value = translator.deserializeValue(table[i]);
                value.forEach((k, v) -> {
                    byte[] kAsBytes = Bytes.toArray(VariableByte.compress(k));
                    byte[] newKey = new byte[];
                    sbMap.put(newKey, v);
                });
                table[i] = null;
            }
        }
        return sbMap;
    }*/

    public static StringPositionsMap join(LightString[] words,
                                          CompactHashMap<LightString, CompactHashMap<Integer, byte[]>>[] maps) {
        StringPositionsMap res = new StringPositionsMap();
        for (LightString word : words) {
            // join positions for one word
            Map<Integer, byte[]> articleIdPositionsMap = new HashMap<>();
            for (CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map : maps) {
                Map<Integer, byte[]> articleIdPositionsMapLocal = map.get(word);
                if (articleIdPositionsMapLocal != null) {
                    articleIdPositionsMap.putAll(articleIdPositionsMapLocal);
                }
            }

            CompactHashMap<Integer, byte[]> compact = new CompactHashMap<>(new IntBytesTranslator());
            compact.putAll(articleIdPositionsMap);
            articleIdPositionsMap = null;
            if (!compact.isEmpty()) {
                res.put(word, compact);
            }
        }
        return res;
    }
}
