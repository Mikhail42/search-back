package org.ionkin.search;

import org.ionkin.Ranking;

public class Snippet {

`    public static String snippet(int[][] wordPositions, int[] idfs, int distance, Page page) {
        int optPos = optimumSnippetPosition(wordPositions, idfs, distance);
        return page.getSnippet(optPos, distance);
    }

    static int optimumSnippetPosition(int[][] wordPositions, int[] idfs, int distance) {
        int bestPos = 0;
        int bestScope = -1;

        int[] is = new int[wordPositions.length];
        int[] pos = Util.mergeSimple(wordPositions);
        for (int p : pos) {
            int scope = 0;
            for (int i = 0; i < distance; i++) {
                for (int k = 0; k < wordPositions.length; k++) {
                    if (is[k] < wordPositions[k].length && wordPositions[k][is[k]] == p + i) {
                        scope += Ranking.tfIdf(idfs[k], wordPositions[k]);
                        is[k]++;
                    }
                }
            }
            if (scope > bestScope) {
                bestPos = p;
                bestScope = scope;
            }
        }

        return bestPos;
    }
}
