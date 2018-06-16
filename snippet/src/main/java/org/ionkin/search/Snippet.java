package org.ionkin.search;

import org.ionkin.Ranking;
import org.ionkin.search.map.StringStringMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Snippet {

    private static final int distance = 20;

    public static Map<Integer, QueryPage> snippets(int[] ids, Map<LightString, Integer> idfs,
                                                Map<LightString, Positions> wordPositionsMap,
                                                StringStringMap lemms) throws IOException {
        Map<Integer, QueryPage> res = new HashMap<>();
        Set<LightString> wordsAsSet = new HashSet<>(idfs.keySet());

        for (int docId : ids) {
            Map<LightString, int[]> wordPos = new HashMap<>();
            wordPositionsMap.forEach((k, v) -> wordPos.put(k, Compressor.decompressVb(v.positions(docId))));
            Page page = TextArticleIterator.readPage(docId);
            String snip = Snippet.snippet(wordPos, idfs, page);
            snip = pretty(snip);
            snip = selectQueryWords(snip, wordsAsSet, lemms);

            res.put(docId, new QueryPage(docId, page.getTitle(), snip));
        }
        return res;
    }

    private static String pretty(String snip) {
        return snip.replaceAll("\\(\\)", "")
                .replaceAll("\\s+", " ")
                .replaceAll(" %", "\u2009%");
    }

    private static String snippet(Map<LightString, int[]> wordPos, Map<LightString, Integer> idfs, Page page) {
        int[][] mat = new int[wordPos.size()][];
        int[] idfsAr = new int[wordPos.size()];
        AtomicInteger ai = new AtomicInteger();
        wordPos.forEach((word, pos) -> {
            mat[ai.get()] = pos;
            idfsAr[ai.get()] = idfs.get(word);
        });

        int optPos = optimumSnippetPosition(mat, idfsAr);
        return getSnippet(page.getContent(), optPos);
    }

    private static int optimumSnippetPosition(int[][] wordPositions, int[] idfs) {
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

    private static String selectQueryWords(String snip, Set<LightString> wordsAsSet, StringStringMap lemms) {
        String[] snipWords = Util.splitPattern.split(snip);
        for (String snipWord : snipWords) {
            LightString norm1 = new LightString(Util.normalize(snipWord));
            LightString norm2 = lemms.getOrDefault(norm1, norm1);
            if (wordsAsSet.contains(norm2)) {
                Pattern pat = Pattern.compile("([^" + Util.wordSymbol + "])" + snipWord + "([^" + Util.wordSymbol + "])");
                Matcher m = pat.matcher(snip);
                if (m.find()) {
                    snip = m.replaceAll("$1<b>" + snipWord + "</b>$2");
                }
            }
        }
        return snip.replaceAll("<b><b>", "<b>")
                .replaceAll("\\s+", " ")
                .replaceAll("</b></b>", "</b>")
                .replaceAll("<b></b>", "")
                .replaceAll("</b> <b>", " ");
    }

    private static String getSnippet(String content, int from) {
        Matcher wordMatcher = Util.wordPattern.matcher(content);
        Matcher splitMatcher = Util.splitPattern.matcher(content);
        int currentIndex = 0;
        int nWord = 0;

        int snipStart = 0;
        int snipEnd = 0;

        while (currentIndex < content.length() && nWord < from + distance) {
            int start = indexOf(wordMatcher, currentIndex);
            if (start == -1) break;
            int end = indexOf(splitMatcher, start);
            if (end == -1) end = content.length();
            currentIndex = end;

            String word = content.substring(start, end);
            String normalWord = Util.normalize(word);
            if (Util.searchable(normalWord)) {
                nWord++;
                if (nWord == from) {
                    snipStart = start;
                }
                if (nWord == from + distance) {
                    snipEnd = end;
                }
            }
        }
        if (snipEnd <= snipStart + 100) {
            snipEnd = Math.min(snipStart + 100, content.length());
        }
        if (snipEnd - snipStart < 100) {
            snipStart = Math.max(snipStart - (100 - (snipEnd - snipStart)), 0);
        }
        return content.substring(snipStart, snipEnd);
    }

    private static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
