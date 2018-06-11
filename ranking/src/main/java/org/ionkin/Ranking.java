package org.ionkin;

import org.ionkin.search.*;
import org.ionkin.search.map.*;

import java.util.HashMap;
import java.util.Map;

public class Ranking {

    private static final int RUSSIAN_WIKI = 1_450_000;

    public static void main(String... args) throws Exception {
        SearchMap positions = new SearchMap(Util.basePath + "positionslemm.sm");
        IndexMap index = new IndexMap(Util.basePath + "indexlemm.im");
    }

    private static byte tfIdf(SearchMap positions, IndexMap index, LightString word, int docId) {
        byte idf = idf(index.get(word).getIndexAsBytes());
        BytesRange pos = positions.get(word).positions(docId);
        return tfIdf(idf, pos);
    }

    private static byte tfIdf(byte idf, BytesRange positions) {
        return (byte) Math.max(idf * tf(positions), Byte.MAX_VALUE);
    }

    /**
     * @param index inverse index for specific term
     * @return IDF of term.
     */
    private static byte idf(BytesRange index) {
        int freq = VariableByte.decompressSize(index);
        return logFreq(RUSSIAN_WIKI / freq);
    }

    private static Map<Integer, Byte> tf(IntBytesMap idPositionsMap) {
        Map<Integer, Byte> res = new HashMap<>();
        idPositionsMap.forEach((docId, positions) -> res.put(docId, tf(positions)));
        return res;
    }

    private static byte tf(BytesRange positions) {
        return logFreq(VariableByte.decompressSize(positions));
    }

    /**
     * @param freq freq. 0 < freq < 2^31
     * @return 1 + log(freq)
     */
    private static byte logFreq(int freq) {
        return (byte)(1 + Math.log(freq));
    }
}
