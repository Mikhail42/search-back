package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.io.IOException;

public class StringIntMap extends CompactHashMap<LightString, Integer> {
    public StringIntMap() {
        super(new StringIntTranslator());
    }

    public StringIntMap(byte[] mapAsBytes) {
        super(new StringIntTranslator(), mapAsBytes);
    }

    public StringIntMap(byte[] mapAsBytes, int from) {
        super(new StringIntTranslator(), mapAsBytes, from);
    }

    public StringIntMap(String filename) throws IOException {
        super(new StringIntTranslator(), filename);
    }
}
