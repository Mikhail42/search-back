package org.ionkin.search;

import org.ionkin.search.map.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.ionkin.search.Statistic.sqr;

public class Collocation {

    public static void main(String... args) throws IOException {
        Collocation col = new Collocation();
        String[] files = new File(Util.textPath).list();
        //    for (String f : files) {
        Set<IntPair> res = col.findFreq(Util.textPath + files[0]); // TODO

        StringBuffer sb = new StringBuffer(1_000_000);
        res.forEach(pair -> {
            LightString s1 = col.isRelat.get(pair.getI1());
            LightString s2 = col.isRelat.get(pair.getI2());
            sb.append(s1.asString());
            sb.append(" ");
            sb.append(s2.asString());
            sb.append("\n");
        });
        FileOutputStream out = new FileOutputStream(files[0]); // TODO
        out.write(sb.toString().getBytes("UTF-8"));
        out.close();
    }

    private static final Logger logger = LoggerFactory.getLogger(Collocation.class);

    private final StringStringMap lemms;

    private final StringIntMap siRelat;
    private final IntStringMap isRelat;
    private final StringStringMap langPart;
    private long allWords = 0;
    private final CompactHashMap<Integer, Integer> wordFreq = new CompactHashMap<>(new IntIntTranslator());

    public Collocation() throws IOException {
        this.langPart = new StringStringMap(Util.basePath + "language-part.ssm");
        /*langPart.forEach((k, v) -> {
            logger.info("{} {}", k.asString(), v.asString());
        });*/
        logger.info("второй: {}", langPart.get(new LightString("второй")).asString());
        logger.info("C: {}", langPart.get(new LightString("с")).asString());
        this.lemms = new StringStringMap(Util.wordLemmPath);
        this.siRelat = new StringIntMap(Util.basePath + "siRelat.si");
        this.isRelat = new IntStringMap(Util.basePath + "isRelat.is");
    }

    public Set<IntPair> findFreq(String absFileName) throws IOException {
        HashMap<IntPair, Integer> pairsFreq = freq(absFileName);
        Set<IntPair> wordIdIdsMap = new HashSet<>();
        pairsFreq.forEach((pair, pairFreq) -> {
            if (pairFreq >= 10 && !checkHipotesByXi(pair, pairFreq) && partFilter(pair)) {
                wordIdIdsMap.add(pair);
            }
        });
        return wordIdIdsMap;
    }

    private boolean checkHipotesByT(IntPair pair, int pairFreq) {
        double p1 = ((double) wordFreq.get(pair.getI1())) / allWords;
        double p2 = ((double) wordFreq.get(pair.getI2())) / allWords;
        double p1p2 = p1 * p2;
        double p12 = ((double) pairFreq) / (allWords - 1);
        return Math.abs(p1p2 - p12) / Math.sqrt(p1p2 * (1 - p1p2) / allWords) < 2.576;
    }

    private boolean checkHipotesByXi(IntPair pair, int pairFreq) {
        long o11 = pairFreq;
        long o12 = wordFreq.get(pair.getI1()) - pairFreq;
        long o21 = wordFreq.get(pair.getI2()) - pairFreq;
        long o22 = allWords - pairFreq;
        double r = allWords * sqr(o11 * o22 - o12 * o21) / (o11 + o12) / (o11 + o21) / (o12 + o22) / (o21 + o22);
        return r < 3.841;
    }

    private boolean partFilter(IntPair pair) {
        return Statistic.partFilter(pair, isRelat, langPart);
    }

    private HashMap<IntPair, Integer> freq(String absFileName) throws IOException {
        WikiParser wikiParser = new WikiParser(absFileName);
        HashMap<IntPair, Integer> wordIdWindowIdsGlob = new HashMap<>();
        wikiParser.getPages().forEach(page -> {
            List<LightString> list = new ArrayList<>();
            String[] words = Util.splitPattern.split(page.getContent());
            for (String word : words) {
                String normalWord = Util.normalize(word);
                if (Util.searchable(normalWord)) {
                    LightString ls = new LightString(normalWord);
                    ls = lemms.getOrDefault(ls, ls);
                    if (siRelat.containsKey(ls)) {// TODO: bug at index
                        wordFreq.merge(siRelat.get(ls), 1, Integer::sum);
                        allWords++;
                        list.add(ls);
                    }
                }
            }
            LightString[] ar = list.toArray(new LightString[0]);
            HashMap<IntPair, Integer> meanStd = freqNeighboringPairs(ar);
            meanStd.forEach((pair, v) -> wordIdWindowIdsGlob.merge(pair, v, Integer::sum));
        });
        return wordIdWindowIdsGlob;
    }

    HashMap<IntPair, Integer> freqNeighboringPairs(LightString[] words) {
        HashMap<IntPair, Integer> res = new HashMap<>();
        for (int i = 0; i < words.length - 1; i++) {
            IntPair p = new IntPair(siRelat.get(words[i]), siRelat.get(words[i + 1]));
            res.merge(p, 1, Integer::sum);
        }
        return res;
    }
}
