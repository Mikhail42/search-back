package org.ionkin.search;

import org.ionkin.search.map.SearchMap;
import org.ionkin.search.map.StringPositionsMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PositionsTest {

    private SearchMap searchMap;

    @Before
    public void before() throws IOException {
        if (searchMap == null) {
            StringPositionsMap positions = new StringPositionsMap(Util.positionsPath);
            searchMap = new SearchMap(positions);
        }
    }

    @Test
    public void serialize() {
        byte[] ser = searchMap.fastSerialization();
        SearchMap sp = new SearchMap(ser);
        byte[] ser2 = sp.fastSerialization();
        assertArrayEquals(ser, ser2);
    }

    @Test
    public void checkPositions() {
        LightString wordToSearch = new LightString(Util.normalize("Россия"));
        Positions wordPositions = searchMap.get(wordToSearch);
        BytesRange poss = wordPositions.positions(9);
        int[] positionIndexes = Compressor.decompressVb(poss);
        Assert.assertEquals(positionIndexes[0], 0);
        Assert.assertTrue(positionIndexes.length > 80); // $wordToSearch used more than 80 times in article #9
        Assert.assertTrue(positionIndexes.length < 1000);
    }
}