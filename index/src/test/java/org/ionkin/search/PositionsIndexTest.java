package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.ionkin.search.map.StringPositionsMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionsIndexTest {

    @Test
    public void writePositionsByFileArticles() throws Exception {
        // you should create two dirs: testText & testPositions, and pass some files to testText from `textPath`
        Util.textPath = Util.basePath + "testText/";
        PositionsIndex.writePositionsByFileArticles(Util.basePath + "testPositions/");
        StringPositionsMap compact = new StringPositionsMap(Util.basePath + "testPositions/AA.spm");
        /*IntBytesMap ibm = compact.get(new LightString("и"));
        BytesRange range = ibm.get(9);
        int[] ids = Compressor.decompressVb(range);

        Positions positions = new Positions(ibm);
        BytesRange range1 = positions.positions(9);
        int[] ints = Compressor.decompressVb(range1);
        int a = 5;*/
    }
}