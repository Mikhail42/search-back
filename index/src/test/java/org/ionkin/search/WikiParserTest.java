package org.ionkin.search;

import com.google.common.base.Utf8;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.model.IntIntPair;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class WikiParserTest {

    @Test
    public void utf8() {
        int id = 7;
        String title = "Литва";
        String content = "<doc id=\"\" title=\"\">";
        int l1 = Utf8.encodedLength(content);
        int l2 = Utf8.encodedLength("<doc id=\"" + id + "\" title=\"" + title + "\">");
        System.err.println(l2 - l1);
        System.err.println(l1- content.length());
    }

    @Test
    public void parsePages() throws IOException {
        WikiParser parser = new WikiParser(Util.textPath + "AA/wiki_00");

        List<Page> pages = parser.getPages();
        Assert.assertTrue(pages.size() > 10);
        Assert.assertEquals(pages.get(0).getId(), 4);
        Assert.assertEquals(pages.get(0).getTitle(), "Базовая статья");
        Assert.assertTrue(pages.get(0).getContent().contains("Базовая статья"));
        Assert.assertTrue(pages.get(1).getContent().startsWith("\nЛитва\n\nЛитв"));

        CompactHashMap<Integer, IntIntPair> docIdToPositions = parser.getDocidPositionMap();
        IntIntPair startAndLength1stPage = docIdToPositions.get(4);
        Page basePage = WikiParser.parsePage(Util.textPath + "AA/wiki_00",
                startAndLength1stPage.first(), startAndLength1stPage.second());
        Assert.assertEquals(pages.get(0).getTitle(), basePage.getTitle());
        Assert.assertEquals(pages.get(0).getContent(), basePage.getContent());
    }
}