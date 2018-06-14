package org.ionkin.search;

import java.util.regex.Matcher;

public class Page {
    private final int id;
    private final String title;
    private final String content;

    public Page(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String generateUrl() {
        return "https://ru.wikipedia.org/wiki?curid=" + id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // TODO: add test
    public String getSnippet(int from, int distance) {
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
        return this.content.substring(snipStart, snipEnd);
    }

    private static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
