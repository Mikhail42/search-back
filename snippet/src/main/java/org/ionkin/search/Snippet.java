package org.ionkin.search;

import org.ionkin.Ranking;
import org.ionkin.search.config.AppConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Snippet {

    private static final int DISTANCE = 20;
    private static final int SNIPPET_LENGTH = AppConfig.snippetLength;

    /**
     * @param ids doc (wiki page) ids
     * @param idfs map of (word from query -> idf)
     * @param wordPositionsMap map of (word from query -> positions)
     * @return map of (page id -> page title and snippet)
     **/
    public static Map<Integer, QueryPage> snippets(int[] ids, Map<LightString, Integer> idfs,
                                                   Map<LightString, Positions> wordPositionsMap) throws IOException {
        Map<Integer, QueryPage> result = new HashMap<>();
        Set<LightString> wordsAsSet = new HashSet<>(idfs.keySet());

        for (int pageId : ids) {
            Map<LightString, int[]> wordToPositionsAtPage = new HashMap<>();
            wordPositionsMap.forEach((k, v) -> wordToPositionsAtPage.put(k, Compressor.decompressVb(v.positions(pageId))));
            Page page = TextArticleIterator.readPage(pageId);
            String snippetText = Snippet.snippet(wordToPositionsAtPage, idfs, page);
            snippetText = pretty(snippetText);
            snippetText = selectQueryWords(snippetText, wordsAsSet);

            result.put(pageId, new QueryPage(pageId, page.getTitle(), snippetText));
        }
        return result;
    }

    static String pretty(String snippetText) {
        return snippetText.replaceAll("\\(\\)", "")
                .replaceAll("\\s+", " ")
                .replaceAll(" %", "\u2009%");
    }

    static String snippet(Map<LightString, int[]> wordToPositions,
                          Map<LightString, Integer> wordToIdf, Page page) {
        int[][] wordIdToPositions = new int[wordToPositions.size()][];
        int[] wordIdToIdf = new int[wordToPositions.size()];
        AtomicInteger wordId = new AtomicInteger();
        wordToPositions.forEach((word, positionsAtPage) -> {
            wordIdToPositions[wordId.get()] = positionsAtPage;
            wordIdToIdf[wordId.getAndIncrement()] = wordToIdf.get(word);
        });

        int optimumPosition = optimumSnippetPosition(wordIdToPositions, wordIdToIdf);
        return getSnippetText(page.getContent(), optimumPosition);
    }

    static int optimumSnippetPosition(int[][] wordIdToPositions, int[] wordIdToIdf) {
        int bestSnippetPosition = 0;
        int bestScope = -1;

        int[] wordIdToCounter = new int[wordIdToPositions.length];
        int[] allPositions = Util.unionAndSort(wordIdToPositions);
        for (int position : allPositions) {
            int scope = 0;
            Set<Integer> added = new HashSet<>();
            for (int diffPosition = 0; diffPosition < DISTANCE; diffPosition++) {
                for (int wordId = 0; wordId < wordIdToPositions.length; wordId++) {
                    if (wordIdToCounter[wordId] < wordIdToPositions[wordId].length
                            && wordIdToPositions[wordId][wordIdToCounter[wordId]] == position + diffPosition) {
                        int tfIdf = Ranking.tfIdf(wordIdToIdf[wordId], wordIdToPositions[wordId]);
                        scope += added.contains(wordId) ? 1 : tfIdf;
                        added.add(wordId);
                        wordIdToCounter[wordId]++;
                    }
                }
            }
            if (scope > bestScope) {
                bestSnippetPosition = position;
                bestScope = scope;
            }
        }

        return bestSnippetPosition;
    }

    // input: some text, Set(text)
    // output: some <b>text</b>
    static String selectQueryWords(String snippetText, Set<LightString> wordsAsSet) {
        String[] snipWords = Util.splitPattern.split(snippetText);
        for (String snippetWord : snipWords) {
            LightString normalized = new LightString(Util.normalize(snippetWord));
            if (wordsAsSet.contains(normalized)) {
                Pattern pat =
                        Pattern.compile("([^" + Util.wordSymbols + "])" + snippetWord + "([^" + Util.wordSymbols + "])");
                Matcher m = pat.matcher(snippetText);
                if (m.find()) {
                    snippetText = m.replaceAll("$1<b>" + snippetWord + "</b>$2");
                }
            }
        }
        return snippetText.replaceAll("\\s+", " ")
                .replaceAll("<b><b>", "<b>")
                .replaceAll("</b></b>", "</b>")
                .replaceAll("<b></b>", "")
                .replaceAll("</b> <b>", " ");
    }

    static String getSnippetText(String content, int from) {
        Matcher wordMatcher = Util.wordPattern.matcher(content);
        Matcher splitMatcher = Util.splitPattern.matcher(content);
        int currentIndex = 0;
        int nWord = 0;

        int snipStart = 0;
        int snipEnd = 0;

        while (currentIndex < content.length() && nWord < from + DISTANCE) {
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
                if (nWord == from + DISTANCE) {
                    snipEnd = end;
                }
            }
        }
        if (snipEnd <= snipStart + SNIPPET_LENGTH) {
            snipEnd = Math.min(snipStart + SNIPPET_LENGTH, content.length());
        }
        if (snipEnd - snipStart < SNIPPET_LENGTH) {
            snipStart = Math.max(snipStart - (SNIPPET_LENGTH - (snipEnd - snipStart)), 0);
        }
        return content.substring(snipStart, snipEnd);
    }

    static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
