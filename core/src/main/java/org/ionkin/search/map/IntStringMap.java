package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.io.IOException;

public class IntStringMap extends CompactHashMap<Integer, LightString> {

    public IntStringMap() {
        super(new IntStringTranslator());
    }

    public IntStringMap(byte[] mapAsBytes) {
        super(new IntStringTranslator(), mapAsBytes);
    }

    public IntStringMap(byte[] mapAsBytes, int from) {
        super(new IntStringTranslator(), mapAsBytes, from);
    }

    public IntStringMap(String filename) throws IOException {
        super(new IntStringTranslator(), filename);
    }
}
