package org.ionkin;

import org.ionkin.search.BytesRange;
import org.ionkin.search.VariableByte;

public class Ranking {

    private static final int RUSSIAN_WIKI = 1_450_000;

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
        if (index == null || index.length() == 0) return 0;
        int freq = VariableByte.decompressSize(index);
        int r = logFreq(RUSSIAN_WIKI / freq);
        return r * r;
    }

    private static int tf(int[] positions) {
        return positions.length == 0 ? 0 : logFreq(positions.length);
    }

    private static int tf(BytesRange positions) {
        return (positions == null || positions.length() == 0) ? 0 : logFreq(VariableByte.decompressSize(positions));
    }

    /**
     * @param freq freq. 0 < freq < 2^31
     * @return 1 + log(freq)
     */
    private static int logFreq(int freq) {
        return (int)Math.round(Math.log1p(freq));
    }
}
