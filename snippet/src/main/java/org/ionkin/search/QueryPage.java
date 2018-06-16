package org.ionkin.search;

public class QueryPage {
    private final int docId;
    private final String title;
    private final String snippet;

    public QueryPage(int docId, String title, String snippet) {
        this.docId = docId;
        this.title = title;
        this.snippet = snippet;
    }

    public String link() {
        return "https://ru.wikipedia.org/wiki?curid=" + docId;
    }

    public int getDocId() {
        return docId;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }
}
