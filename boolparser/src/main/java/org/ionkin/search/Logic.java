package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.Ranking;
import org.ionkin.search.map.IndexMap;
import org.ionkin.search.map.SearchMap;

import java.util.*;

public class Logic {

    static int[] or(int[] ar1, int[] ar2, int count) {
        List<Integer> list = new ArrayList<>();
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length && n < count) {
            if (ar1[i1] > ar2[i2]) {
                list.add(ar2[i2]);
                i2++;
            } else if (ar1[i1] < ar2[i2]) {
                list.add(ar1[i1]);
                i1++;
            } else {
                list.add(ar1[i1]);
                i1++;
                i2++;
            }
            n++;
        }
        while (i1 < ar1.length && n < count) {
            list.add(ar1[i1]);
            i1++;
            n++;
        }
        while (i2 < ar2.length && n < count) {
            list.add(ar2[i2]);
            i2++;
            n++;
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    static int[] and(int[] ar1, int[] ar2, int count) {
        List<Integer> list = new ArrayList<>();
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length && n < count) {
            if (ar1[i1] > ar2[i2]) i2++;
            else if (ar1[i1] < ar2[i2]) i1++;
            else {
                list.add(ar1[i1]);
                i1++;
                i2++;
                n++;
            }
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    static int[] firstAndNotSecond(int[] ar1, int[] ar2, int count) {
        List<Integer> list = new ArrayList<>();
        int n = 0;
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length && n < count) {
            if (ar1[i1] > ar2[i2]) {
                i2++;
            } else if (ar1[i1] < ar2[i2]) {
                list.add(ar1[i1]);
                i1++;
                n++;
            } else {
                i1++;
                i2++;
            }
        }
        while (i1 < ar1.length && n < count) {
            list.add(ar1[i1]);
            i1++;
            n++;
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    /**
     * find indices for quote with max distance.
     * e.g.: for <<Слово о Игореве>> / 4
     * or <<Слово о полку Игореве>> / 4
     *
     * @param wordIndices      list of word indices. Used as first filter
     * @param wordPositionsMap list of word positions
     * @param count            max count of returned indices
     * @param distance         max distance between first and last word at quotes
     * @return index's where located this quote
     */
    static int[] andQuotes(Index[] wordIndices, Positions[] wordPositionsMap, int count, int distance) {
        IntArray res = new IntArray();
        int n = wordIndices.length;
        // check that all indices is not empty
        boolean isNotOutOfBound = true;
        for (Index index : wordIndices) {
            isNotOutOfBound &= index.hasNext();
        }
        while (isNotOutOfBound && res.size() < count) {
            // do search while doc id is different and no one of word index is not out of bound
            int docId = 0;
            for (Index index : wordIndices) {
                docId = Math.max(docId, index.nextDocIdWithoutInc());
            }

            // check docId is common
            boolean eq = true;
            for (Index index : wordIndices) {
                eq &= index.containsDocWithGoToEffect(docId);
            }

            if (eq) { // add if position's distance is small
                int[][] wordPositions = new int[n][];
                for (int i = 0; i < n; i++) {
                    BytesRange poss = wordPositionsMap[i].positions(docId);
                    wordPositions[i] = Compressor.decompressVb(poss);
                }
                if (isQuote(wordPositions, distance)) {
                    res.add(docId);
                }
                for (Index index : wordIndices) {
                    if (index.hasNext()) index.nextDocId();
                }
            }
            // check out of bound state
            for (Index index : wordIndices) {
                isNotOutOfBound &= index.hasNext();
            }
        }

        for (Index index : wordIndices) {
            index.goToStartPosition();
        }

        return res.getCopy();
    }

    static int[] rankingTfIdf(int[] docIds0, LightString[] words, SearchMap searchMap, IndexMap indexMap, int count) {
        TreeMap<Integer, List<Integer>> indScope = new TreeMap<>(Integer::compare);
        indScope.put(0, new LinkedList<>());

        int n = words.length;
        int nInScope = 0;

        byte[] idfs = new byte[n];
        Map<LightString, Positions> wordPositionsMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            idfs[i] = Ranking.idf(indexMap.get(words[i]).getIndexAsBytes());
            wordPositionsMap.put(words[i], searchMap.get(words[i]));
        }

        for (int docId : docIds0) {
            int scope = 0;
            for (int i = 0; i < n; i++) {
                BytesRange poss = wordPositionsMap.get(words[i]).positions(docId);
                if (poss.length() != 0) {
                    scope += Ranking.tfIdf(idfs[i], poss);
                }
            }

            if (scope >= indScope.firstKey()) {
                List<Integer> list = indScope.getOrDefault(scope, new LinkedList<>());
                list.add(docId);
                nInScope++;

                indScope.put(scope, list);
            }
            if (nInScope > count) {
                int s = indScope.firstEntry().getValue().size();
                if (nInScope - s >= count) {
                    indScope.pollFirstEntry();
                    nInScope -= s;
                }
            }
        }

        IntArray res = new IntArray();
        indScope.forEach((k, v) -> {
            res.add(Ints.toArray(v));
        });
        int[] result = res.getCopy();
        Arrays.sort(result);
        for (int i=0; i<result.length/2; i++) {
            int t = result[i];
            result[i] = result[result.length - 1 - i];
            result[result.length - 1 - i] = t;
        }
        return (result.length > count) ? Arrays.copyOf(result, count) : result;
    }

    /**
     * find indices for quote with max distance.
     * e.g.: for <<Слово о Игореве>> / 4
     * or <<Слово о полку Игореве>> / 4
     *
     * @param wordIndices      list of word indices. Used as first filter
     * @param wordPositionsMap list of word positions
     * @param count            max count of returned indices
     * @param distance         max distance between first and last word at quotes
     * @return index's where located this quote
     */
    @Deprecated
    static int[] andQuotes(int[][] wordIndices, Positions[] wordPositionsMap, int count, int distance) {
        IntArray res = new IntArray();
        int n = wordIndices.length;
        int[] is = new int[n]; // indices

        // check that all indices is not empty
        boolean isNotOutOfBound = true;
        for (int k = 0; k < n; k++) {
            isNotOutOfBound &= (is[k] < wordIndices[k].length);
        }
        while (isNotOutOfBound && res.size() < count) {
            // do search while doc id is different and no one of word index is not out of bound
            int docId = 0;
            for (int k = 0; k < n; k++) {
                docId = Math.max(docId, wordIndices[k][is[k]]);
            }
            for (int k = 0; k < n; k++) {
                while (isNotOutOfBound && (wordIndices[k][is[k]] < docId)) {
                    is[k]++;
                    isNotOutOfBound &= (is[k] < wordIndices[k].length);
                }
            }
            if (isNotOutOfBound) {
                // check docId is common
                boolean eq = true;
                for (int k = 0; k < n; k++) {
                    eq &= (docId == wordIndices[k][is[k]]);
                }
                if (eq) {
                    // add if position's distance is small
                    int[][] wordPositions = new int[n][];
                    for (int i = 0; i < n; i++) {
                        BytesRange poss = wordPositionsMap[i].positions(docId);
                        wordPositions[i] = Compressor.decompressVb(poss);
                    }
                    if (isQuote(wordPositions, distance)) {
                        res.add(docId);
                    }
                    for (int k = 0; k < n; k++) {
                        is[k]++;
                        isNotOutOfBound &= (is[k] < wordIndices[k].length);
                    }
                }
            }
        }
        return res.getCopy();
    }

    /**
     * @param wordPositions matrix of word positions. Each row is represent a word positions at document
     * @param distance      max distance between first and last words
     * @return is @distance more or equals then distance between first and last words at quotes
     */
    static boolean isQuote(int[][] wordPositions, int distance) {
        if (distance < wordPositions.length) return false;
        int[] is = new int[wordPositions.length];
        boolean isNotOutOfBound = true;
        for (int k = 0; k < wordPositions.length; k++) {
            isNotOutOfBound &= (is[k] < wordPositions[k].length);
        }
        while (isNotOutOfBound) {
            for (int k = 1; k < wordPositions.length; k++) {
                while (isNotOutOfBound && (wordPositions[k][is[k]] <= wordPositions[k - 1][is[k - 1]])) {
                    is[k]++;
                    isNotOutOfBound &= (is[k] < wordPositions[k].length);
                }
            }
            if (isNotOutOfBound) {
                int p = wordPositions.length - 1;
                if (wordPositions[p][is[p]] - wordPositions[0][is[0]] <= distance) {
                    return true;
                } else {
                    is[0]++;
                    isNotOutOfBound &= (is[0] < wordPositions[0].length);
                }
            }
        }
        return false;
    }
}
