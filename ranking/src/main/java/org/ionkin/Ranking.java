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

    private static int tfIdf(SearchMap positions, IndexMap index, LightString word, int docId) {
        int idf = idf(index.get(word).getIndexAsBytes());
        BytesRange pos = positions.get(word).positions(docId);
        return tfIdf(idf, pos);
    }

    public static int tfIdf(int idf, int[] positions) {
        return idf * tf(positions);
    }

    public static int tfIdf(int idf, BytesRange positions) {
        return idf * tf(positions);
    }

    /**
     * @param index inverse index for specific term
     * @return IDF of term.
     */
    public static int idf(BytesRange index) {
        int freq = VariableByte.decompressSize(index);
        return logFreq(RUSSIAN_WIKI / freq);
    }

    private static Map<Integer, Integer> tf(IntBytesMap idPositionsMap) {
        Map<Integer, Integer> res = new HashMap<>();
        idPositionsMap.forEach((docId, positions) -> res.put(docId, tf(positions)));
        return res;
    }

    private static int tf(int[] positions) {
        return logFreq(positions.length);
    }

    private static int tf(BytesRange positions) {
        return logFreq(VariableByte.decompressSize(positions));
    }

    /**
     * @param freq freq. 0 < freq < 2^31
     * @return 1 + log(freq)
     */
    private static int logFreq(int freq) {
        return (int)(1 + Math.log(freq));
    }
}
