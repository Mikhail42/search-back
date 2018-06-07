package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;

import java.util.Iterator;
import java.util.Map;

public class Main {
    public static void main(String... args) throws Exception {
        Iterator<Page> iterator = TextArticleIterator.articleTextIterator(Util.basePath + "testText.txt");
        //Page ilichPage = iterator.next();
        //Map<LightString, List<Integer>> positions = PositionsIndex.positionsAtPage(ilichPage.getContent());
        Map<LightString, IntBytesMap> local = PositionsIndex.positions(iterator);
        IntBytesMap ibm = local.get(new LightString("война"));
        BytesRange range = ibm.get(256);
        int[] ids256 = Compressor.decompressVb(range);
        //int[] ids = Ints.toArray(positions.get(new LightString("война")));
        int a = 5;
    }
}
