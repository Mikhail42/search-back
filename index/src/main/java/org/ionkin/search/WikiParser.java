package org.ionkin.search;

import com.google.common.base.Utf8;
import org.ionkin.search.model.IntIntPair;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.IntIntIntTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiParser {
    private static final Logger logger = LoggerFactory.getLogger(WikiParser.class);
    //<doc id="4" url="https://ru.wikipedia.org/wiki?curid=4" title="Базовая статья">
    // Базовая статья
    //</doc>
    private static final String docPatternStr =
            "<doc id=\"(\\d+)\" url=\"https://ru\\.wikipedia\\.org/wiki\\?curid=\\d+\" title=\"([^\"]+)\">(.*?(?=</doc>))</doc>";
    private static final Pattern docPattern = Pattern.compile(docPatternStr, Pattern.DOTALL);

    private final String fileContent;
    private final ArrayList<Page> pages = new ArrayList<>();
    private final CompactHashMap<Integer, IntIntPair> docidPositionMap =
            new CompactHashMap<>(new IntIntIntTranslator());

    public WikiParser(String filename) throws IOException {
        fileContent = new String(IO.read(filename), StandardCharsets.UTF_8);
        parseAllPages();
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public CompactHashMap<Integer, IntIntPair> getDocidPositionMap() {
        return docidPositionMap;
    }

    public static Page parsePage(String filename, int startPosition, int length) throws IOException {
        String content = new String(IO.read(filename, startPosition, length), StandardCharsets.UTF_8);
        Matcher docMatcher = docPattern.matcher(content);
        if (docMatcher.find()) {
            int id = Integer.parseInt(docMatcher.group(1));
            String title = docMatcher.group(2);
            String text = docMatcher.group(3);
            return new Page(id, title, text);
        } else {
            logger.warn("Can't parse page in {}. position (in bytes): {}, length: {}", filename, startPosition, length);
            return null;
        }
    }

    private void parseAllPages() {
        Matcher docMatcher = docPattern.matcher(fileContent);
        int currentByteLength = 0;
        while (docMatcher.find()) {
            int id = Integer.parseInt(docMatcher.group(1));
            String title = docMatcher.group(2);
            String text = docMatcher.group(3);
            pages.add(new Page(id, title, text));

            String header = "<doc id=\"" + id + "\" url=\"https://ru.wikipedia.org/wiki?curid=" + id
                    + "\" title=\"" + title + "\">";
            int head = Utf8.encodedLength(header);
            int tail = Utf8.encodedLength("</doc>\n");

            int start = currentByteLength;
            int length = head + Utf8.encodedLength(text) + tail;
            currentByteLength += length;

            docidPositionMap.put(id, new IntIntPair(start, length));
        }
    }
}
