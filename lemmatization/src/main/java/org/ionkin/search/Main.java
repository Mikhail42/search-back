package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.ionkin.search.map.StringBytesMap;
import org.ionkin.search.map.StringPositionsMap;
import org.ionkin.search.map.StringStringMap;

public class Main {

    public static void main(String... args) throws Exception {
        //Stream.of("маи").map(Porter::stem).forEach(System.out::println);
        //udpipe_java.setLibraryPath("C:/Users/Misha/Downloads/udpipe-1.2.0-bin/udpipe-1.2.0-bin/bin-win64/java/udpipe_java.dll");
        lemmPositions();
    }

    public static void lemmIndex() throws Exception {
        StringBytesMap sbm = new StringBytesMap(Util.indexPath);
        StringStringMap dic = new StringStringMap(Util.wordLemmPath);

        StringBytesMap lemmSbm = new StringBytesMap();
        sbm.forEach((w, ind) -> {
            LightString norm = dic.getOrDefault(w, w);
            BytesRange old = lemmSbm.get(norm);
            if (old == null) {
                lemmSbm.put(norm, ind);
            } else {
                lemmSbm.put(norm, joinRange(old, ind));
            }
        });
        lemmSbm.write(Util.basePath + "indexlemm.sbm");
    }

    public static void lemmPositions() throws Exception {
        StringPositionsMap sbm = new StringPositionsMap(Util.positionsPath);
        StringStringMap dic = new StringStringMap(Util.wordLemmPath);

        StringPositionsMap lemmSbm = new StringPositionsMap();
        sbm.forEach((w, ind) -> {
            LightString norm = dic.getOrDefault(w, w);
            IntBytesMap old = lemmSbm.get(norm);
            if (old == null) {
                lemmSbm.put(norm, ind);
            } else {
                join(old, ind);
                lemmSbm.put(norm, old);
            }
        });
        lemmSbm.write(Util.basePath + "positionslemm.spm");
    }

    private static void join(IntBytesMap ibm1, IntBytesMap ibm2) {
        ibm2.forEach((id, pos2) -> {
            BytesRange r1 = ibm1.get(id);
            if (r1 == null) {
                ibm1.put(id, pos2);
            } else {
                ibm1.put(id, joinRange(pos2, r1));
            }
        });
    }

    private static BytesRange joinRange(BytesRange b1, BytesRange b2) {
        int[] uncompOld = Compressor.decompressVb(b1);
        int[] uncomp = Compressor.decompressVb(b2);
        int[] res = Util.merge(uncomp, uncompOld);
        byte[] comp = Compressor.compressVbWithoutMemory(res);
        return new BytesRange(comp);
    }
}
