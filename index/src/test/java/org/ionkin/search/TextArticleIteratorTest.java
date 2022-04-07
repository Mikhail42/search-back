package org.ionkin.search;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
        File textPath = new File(Util.textPath);
        int dirs = (int)Arrays.stream(textPath.listFiles()).filter(File::isDirectory).count();
        assertTrue(dirs > 0);
        assertEquals(dirs * 100, map.size()); // wikiextractor generate 100 files per dir (maybe expect last dir)
        assertEquals(map.get(4), "AA/wiki_00"); // first id in Russian Wikipedia is 4
    }

    @Test
    public void iterate() {
        Iterator<Page> pageIterator = TextArticleIterator.articleTextIterator();
        assertNotNull(pageIterator);
        assertTrue(pageIterator.hasNext());
        Page firstPage = pageIterator.next();
        assertEquals(4, firstPage.getId());
        assertEquals("Базовая статья", firstPage.getTitle());
    }
}