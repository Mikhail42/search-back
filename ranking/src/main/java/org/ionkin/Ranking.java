package org.ionkin;

import org.ionkin.search.BytesRange;
import org.ionkin.search.Util;
import org.ionkin.search.VariableByte;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.IntTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Ranking {
    private static final Logger logger = LoggerFactory.getLogger(Ranking.class);

    private static int numberOfPages;

    static {
        try {
            CompactHashSet<Integer> pageIds = CompactHashSet.read(Util.docIdsPath, new IntTranslator());
            numberOfPages = pageIds.size();
        } catch (IOException e) {
            logger.error("Can't read pages ids. Use default value", e);
            numberOfPages = 10_000_000;
            e.printStackTrace();
        }
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
        if (index == null || index.length() == 0) return 0;
        int freq = VariableByte.decompressSize(index);
        int r = logFreq(numberOfPages / freq);
        return r * r * r;
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
