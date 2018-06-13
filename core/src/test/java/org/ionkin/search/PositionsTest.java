package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.ionkin.search.map.SearchMap;
import org.ionkin.search.map.StringBytesMap;
import org.ionkin.search.map.StringPositionsMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PositionsTest {

    private SearchMap positions;
/*
    @Before
    public void before() throws IOException {
        positions = new SearchMap(Util.testPositionsPath);
    }

    @Test
    public void serialize() {
        byte[] ser = positions.fastSerialization();
        SearchMap sp = new SearchMap(ser);
        byte[] ser2 = sp.fastSerialization();
        Assert.assertTrue(Arrays.equals(ser, ser2));
    }

    @Test
    public void a() {
        Positions andPos = positions.get(new LightString("и"));
        BytesRange poss = andPos.positions(9);
        int[] ps = Compressor.decompressVb(poss);
        Arrays.toString(ps);
    }*/
}