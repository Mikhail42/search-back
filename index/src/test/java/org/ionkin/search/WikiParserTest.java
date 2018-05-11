package org.ionkin.search;

import com.google.common.base.Utf8;
import org.junit.Test;

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
}