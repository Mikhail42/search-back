package org.ionkin.search;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.ionkin.search.map.IntBytesMap;
import org.ionkin.search.map.StringBytesMap;
import org.ionkin.search.map.StringPositionsMap;
import org.ionkin.search.map.StringStringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static String modelPath = Util.basePath + "russian.par";
    private static String encoding = "utf-8";

    public static void main(String... args) throws Exception {
        lemmPositions();
        //StringBytesMap sbm = new StringBytesMap(Util.indexPath);
        /*StringStringMap oldLemm = new StringStringMap(Util.wordLemmPath);
        String[] words = new String[oldLemm.size()];
        AtomicInteger ai = new AtomicInteger();
        oldLemm.keySet().forEach(k -> words[ai.getAndIncrement()] = k.asString());
        System.gc();
        logger.info("words are read. size: {}. Expected time is {} minutes", words.length, (words.length * 3.6 / 6000));

        System.setProperty("treetagger.home", Util.basePath + "/TreeTagger");
        TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<>();
        try {
            StringStringMap ssm = new StringStringMap();

            tt.setModel(modelPath + ":" + encoding);
            logger.info("model are read");
            AtomicInteger ai2 = new AtomicInteger();
            tt.setHandler((token, pos, lemma) -> {
                if (ai2.incrementAndGet() % 10_000 == 0) logger.info("ai={}", ai2.get());
                if (!token.equals(lemma)) ssm.put(new LightString(token), new LightString(lemma));
            });
            logger.info("start process");
            tt.process(words);
            logger.info("end process. try write result");
            ssm.write(Util.basePath + "lemmtt4j.ssm");
            logger.info("OK");
        } finally {
            tt.destroy();
        }*/
    }

    public static void lemmIndex() throws Exception {
        StringBytesMap sbm = new StringBytesMap(Util.indexPath);
        StringStringMap lemm = new StringStringMap(Util.wordLemmPath);

        StringBytesMap lemmSbm = new StringBytesMap();
        sbm.forEach((w, ind) -> {
            LightString norm = lemm.getOrDefault(w, w);
            BytesRange old = lemmSbm.get(norm);
            if (old == null) {
                lemmSbm.put(norm, ind);
            } else {
                lemmSbm.put(norm, joinRange(old, ind));
            }
        });
        lemmSbm.write(Util.basePath + "indexlemm.im");
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
        lemmSbm.write(Util.basePath + "positionslemm.sm");
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
