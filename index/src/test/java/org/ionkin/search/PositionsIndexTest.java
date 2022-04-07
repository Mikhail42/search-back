package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.ionkin.search.map.StringPositionsMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PositionsIndexTest {

    @Before
    public void before() {
        if (!new File(Util.positionsPath).exists()) {
            PositionsIndex.init();
        }
    }

    @Test
    public void checkPositionIndexTest() throws IOException {
        StringPositionsMap index = PositionsIndex.readIndex();
        IntBytesMap intBytesMap = index.get(new LightString("слон"));
        int[] partOfExpectedIds = {
                10, 11, 1490, 2489, 2767, 2822, 2846, 2947, 8072, 8743, 8805, 9008, 9755, 10657, 11487, 15577, 17060,
                18833, 19389, 19472, 19525, 19834, 19883, 20045, 20670, 22159, 22524, 22732, 22738, 23396, 23860, 24391,
                24703, 24874, 25088, 30268, 30447, 34094, 34719, 40486, 40625, 45865, 45887, 49614, 49663, 49788, 54409,
                55691, 55693, 56308, 57813, 60835, 61640, 63179, 63598, 67494, 67939, 71955, 75204, 75374, 77148, 77514
        };
        for (int pageId : partOfExpectedIds) {
            Assert.assertTrue(intBytesMap.containsKey(pageId));
            String content = TextArticleIterator.readPage(pageId).getContent();
            Assert.assertTrue(content.toLowerCase().contains("слон"));
            BytesRange positionBytes = intBytesMap.get(pageId);
            int[] positions = Compressor.decompressVb(positionBytes);
            Assert.assertTrue(positions.length > 0);
        }
    }

}