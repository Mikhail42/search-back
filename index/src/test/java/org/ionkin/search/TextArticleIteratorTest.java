package org.ionkin.search;

import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class TextArticleIteratorTest {

    @Test
    public void readPage() throws IOException {
        Page mamontPage = TextArticleIterator.readPage(11);
        assertEquals(11, mamontPage.getId());
        assertEquals("Мамонты", mamontPage.getTitle());
        assertTrue(mamontPage.getContent().startsWith("\nМамонты\n\nМа́монты () — вымерший род млекопитающих"));
    }

    @Test
    public void readFirstDocidFilenameMap() throws Exception {
        Map<Integer, String> map = TextArticleIterator.readFirstDocidFilenameMap();
        assertEquals(map.get(7), "AA f");
    }

    @Test
    public void iterate() {
        Iterator<Page> pageIterator = TextArticleIterator.articleTextIterator();
        assertNotNull(pageIterator);
        assertTrue(pageIterator.hasNext());
        Page litva = pageIterator.next();
        assertEquals(7, litva.getId());
        assertEquals("Литва", litva.getTitle());
    }
}