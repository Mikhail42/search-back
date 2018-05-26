package org.ionkin.search;

import org.ionkin.search.map.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class QuoteAnalysis {
    private static Logger logger = LoggerFactory.getLogger(QuoteAnalysis.class);

    private final StringPositionsMap index;

    public static void main(String... args) throws Exception {
        QuoteAnalysis evaluator = load();
    }

    public static QuoteAnalysis load() throws IOException {
        StringPositionsMap index = new StringPositionsMap();
        index.read(Util.positionsPath + "Fast");
        return new QuoteAnalysis(index);
    }

    public QuoteAnalysis(StringPositionsMap index) {
        this.index = index;
        logger.debug("QuoteAnalysis created");
    }

    /*public int[] find(List<LightString> seq) {


    }*/
}
