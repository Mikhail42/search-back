package org.ionkin.search.map;

import java.io.IOException;

public class IntBytesMap extends CompactHashMap<Integer, byte[]> {

    public IntBytesMap() {
        super(new IntBytesTranslator());
    }

    public IntBytesMap(byte[] mapAsBytes) {
        super(new IntBytesTranslator(), mapAsBytes);
    }

    public IntBytesMap(String filename) throws IOException {
        super(new IntBytesTranslator(), filename);
    }
}
