package org.ionkin.search.map;

import org.ionkin.search.BytesRange;

import java.io.IOException;

public class IntBytesMap extends CompactHashMap<Integer, BytesRange> {

    public IntBytesMap() {
        super(new IntBytesTranslator());
    }

    public IntBytesMap(byte[] mapAsBytes) {
        super(new IntBytesTranslator(), mapAsBytes);
    }

    public IntBytesMap(byte[] mapAsBytes, int from) {
        super(new IntBytesTranslator(), mapAsBytes, from);
    }

    public IntBytesMap(String filename) throws IOException {
        super(new IntBytesTranslator(), filename);
    }
}
