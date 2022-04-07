package org.ionkin.search.map;

import java.io.IOException;

public class BytesBytesMap extends CompactHashMap<byte[], byte[]> {
    public BytesBytesMap() {
        super(new BytesBytesTranslator());
    }

    public BytesBytesMap(String filename) throws IOException {
        super(new BytesBytesTranslator(), filename);
    }
}
