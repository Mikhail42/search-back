package org.ionkin.search;

import org.ionkin.search.map.StringPositionsMap;

public class Main {
    public static void main(String... args) throws Exception {
        /*CompactHashMap<Integer, Pair<Integer, Integer>> map =
                CompactHashMap.read(Util.basePath + "docPositions.chmiiiFast", new IntIntIntTranslator());
        int[] docids = Ints.toArray(map.keySet());
        Arrays.sort(docids);
        IO.write(Compressor.compressVbWithoutMemory(docids), Util.basePath +  "docids.chsi");*/
        //CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> index = CompactHashMap.read(Util.positionsPath, new StringPositionsMapTranslator());
        //index.write(Util.positionsPath + "Fast");
        //StringPositionsMap index = new StringPositionsMap();
        //index.read(Util.positionsPath + "Fast");
    }
}
