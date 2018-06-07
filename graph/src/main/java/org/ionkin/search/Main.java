package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringIntTranslator;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static final String basePath = "/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/";

    public static void main(String... args) throws Exception {
        CompactHashMap<LightString, Integer> tokens = new CompactHashMap<>(new StringIntTranslator());
        tokens.read(basePath + "freq/allTokens");
        logger.debug("file is readed");
        Collection<Integer> values = tokens.values();
        tokens = null;
        logger.debug("values is getted");
        int[] ar = new int[values.size()];
        int i = 0;
        for (int v : values) {
            ar[i++] = v;
        }
        Arrays.sort(ar);
        values = null;
        logger.debug("values sorted");

        final MyChart myChart = new MyChart(ar, 70);
        myChart.setAlwaysOnTop(true);
        myChart.pack();
        RefineryUtilities.centerFrameOnScreen(myChart);
        myChart.setVisible(true);
        logger.debug("myChart created");
    }
}
