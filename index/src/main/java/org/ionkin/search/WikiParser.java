package org.ionkin.search;

import com.google.common.base.Utf8;
import org.ionkin.search.model.IntIntPair;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.IntIntIntTranslator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiParser {

    private static final String pat = "<doc id=\"(\\d+)\" title=\"([^\"]+)\">(.*?(?=</doc>))</doc>";
    private static final Pattern pattern = Pattern.compile(pat, Pattern.DOTALL);

    private final String fileContent;
    private final ArrayList<Page> pages = new ArrayList<>();
    private final CompactHashMap<Integer, IntIntPair> docidPositionMap =
            new CompactHashMap<>(new IntIntIntTranslator());

    public WikiParser(String filename) throws IOException {
        fileContent = new String(IO.read(filename), StandardCharsets.UTF_8);
        parseAllPages();
    }

    public static Page parsePage(String filename, int startPosition, int length) throws IOException {
        String content = new String(IO.read(filename, startPosition, length), StandardCharsets.UTF_8);
        Matcher m = pattern.matcher(content);
        if (m.find()) {
            int id = Integer.parseInt(m.group(1));
            String title = m.group(2);
            String text = m.group(3);
            return new Page(id, title, text);
        } else {
            return null;
        }
    }

    private void parseAllPages() {
        Matcher m = pattern.matcher(fileContent);
        int currentByteLength = 0;
        while (m.find()) {
            int id = Integer.parseInt(m.group(1));
            String title = m.group(2);
            String text = m.group(3);
            pages.add(new Page(id, title, text));

            int head = Utf8.encodedLength("<doc id=\"" + id + "\" title=\"" + title + "\">");
            int tail = Utf8.encodedLength("</doc>\n");

            int start = currentByteLength;
            int length = head + Utf8.encodedLength(text) + tail;
            currentByteLength += length;

            docidPositionMap.put(id, new IntIntPair(start, length));
        }
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public CompactHashMap<Integer, IntIntPair> getDocidPositionMap() {
        return docidPositionMap;
    }
}
