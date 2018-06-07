package org.ionkin.search.map;

import org.ionkin.search.LightString;
import org.ionkin.search.Positions;

import java.io.IOException;

public class SearchMap extends CompactHashMap<LightString, Positions>{
    public SearchMap() {
        super(new SearchTranslator());
    }

    public SearchMap(StringPositionsMap positions) {
        super(new SearchTranslator());
        positions.forEach((k, v) -> {
            Positions ps = new Positions(v);
            put(k, ps);
        });
    }

    public SearchMap(byte[] mapAsBytes) {
        super(new SearchTranslator(), mapAsBytes);
    }

    public SearchMap(byte[] mapAsBytes, int from) {
        super(new SearchTranslator(), mapAsBytes, from);
    }

    public SearchMap(String filename) throws IOException {
        super(new SearchTranslator(), filename);
    }
}
