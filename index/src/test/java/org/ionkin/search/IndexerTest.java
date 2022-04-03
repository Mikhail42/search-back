package org.ionkin.search;

import org.ionkin.search.map.StringBytesMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class IndexerTest {

    @Before
    public void before() {
        Indexer.init();
    }

    @Test
    public void indexTest() throws IOException {
        StringBytesMap index = Indexer.readIndex();
        BytesRange bytes = index.get(new LightString("слон"));
        int[] pageIds = Compressor.decompressVb(bytes);
        int[] partOfExpectedIds = {
                10, 11, 1490, 2489, 2767, 2822, 2846, 2947, 8072, 8743, 8805, 9008, 9755, 10657, 11487, 15577, 17060,
                18833, 19389, 19472, 19525, 19834, 19883, 20045, 20670, 22159, 22524, 22732, 22738, 23396, 23860, 24391,
                24703, 24874, 25088, 30268, 30447, 34094, 34719, 40486, 40625, 45865, 45887, 49614, 49663, 49788, 54409,
                55691, 55693, 56308, 57813, 60835, 61640, 63179, 63598, 67494, 67939, 71955, 75204, 75374, 77148, 77514
        };
        int[] subPageIds = Arrays.copyOf(pageIds, partOfExpectedIds.length);
        Assert.assertArrayEquals(partOfExpectedIds, subPageIds);

        Assert.assertTrue(TextArticleIterator.readPage(pageIds[23]).getContent().contains("слон"));
    }

    @Test
    public void titleIndexTest() throws IOException {
        StringBytesMap titleIndex = Indexer.readTitleIndex();
        BytesRange bytes = titleIndex.get(new LightString("слон"));
        int[] pageIds = Compressor.decompressVb(bytes);
        int[] partOfExpectedIds = {20045, 55693};
        int[] subPageIds = Arrays.copyOf(pageIds, partOfExpectedIds.length);
        Assert.assertArrayEquals(partOfExpectedIds, subPageIds);

        Assert.assertEquals("Слон (шахматы)", TextArticleIterator.readPage(20045).getTitle());
        Assert.assertEquals("Боевой слон", TextArticleIterator.readPage(55693).getTitle());
    }
}