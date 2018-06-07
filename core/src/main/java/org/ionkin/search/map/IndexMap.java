package org.ionkin.search.map;

import org.ionkin.search.Index;
import org.ionkin.search.LightString;

import java.io.IOException;

public class IndexMap extends CompactHashMap<LightString, Index> {
    public IndexMap() {
        super(new IndexMapTranslator());
    }

    public IndexMap(byte[] mapAsBytes) {
        super(new IndexMapTranslator(), mapAsBytes);
    }

    public IndexMap(byte[] mapAsBytes, int from) {
        super(new IndexMapTranslator(), mapAsBytes, from);
    }

    public IndexMap(String filename) throws IOException {
        super(new IndexMapTranslator(), filename);
    }

    public IndexMap(StringBytesMap index) {
        this();
        index.forEach((k, v) -> put(k, Index.fromOldIndex(v)));
    }
}
