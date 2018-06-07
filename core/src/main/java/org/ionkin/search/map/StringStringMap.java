package org.ionkin.search.map;

import org.ionkin.search.LightString;

import java.io.IOException;

public class StringStringMap extends CompactHashMap<LightString, LightString> {

    public StringStringMap() {
        super(new StringStringTranslator());
    }

    public StringStringMap(byte[] mapAsBytes) {
        super(new StringStringTranslator(), mapAsBytes);
    }

    public StringStringMap(byte[] mapAsBytes, int from) {
        super(new StringStringTranslator(), mapAsBytes, from);
    }

    public StringStringMap(String filename) throws IOException {
        super(new StringStringTranslator(), filename);
    }

    public static StringStringMap join(StringStringMap[] maps) {
        StringStringMap res = new StringStringMap();
        for (StringStringMap m : maps) {
            res.putAll(m);
        }
        return res;
    }
}
