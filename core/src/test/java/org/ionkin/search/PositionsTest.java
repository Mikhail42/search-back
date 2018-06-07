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
    }

    @Test
    public void deserialize() {
        ByteArray buf = new ByteArray();

        buf.add(VariableByte.compress(1)); // index length

        buf.add(VariableByte.compress(15)); // index

        int[] positions = new int[]{2, 10};
        byte[] bytes = Compressor.compressVbWithMemory(positions);
        buf.add(VariableByte.compress(bytes.length));
        buf.add(bytes);

        byte[] copy = buf.getCopy();

        Positions pos = Positions.deserialize(copy, 0);
        BytesRange range = pos.positions(15);

        assertArrayEquals(Compressor.decompressVb(range), positions);
    }

    @Test
    public void create() {
        IntBytesMap intBytesMap = new IntBytesMap();
        int[] poss1 = new int[]{2, 10};
        int[] poss2 = new int[]{2, 10};

        intBytesMap.put(15, new BytesRange(Compressor.compressVbWithMemory(poss1)));
        intBytesMap.put(25, new BytesRange(Compressor.compressVbWithMemory(poss2)));

        Positions pos = new Positions(intBytesMap);
        assertArrayEquals(Compressor.decompressVb(pos.positions(15)), poss1);
        assertArrayEquals(Compressor.decompressVb(pos.positions(25)), poss2);
    }
}