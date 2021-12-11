package org.ionkin.search;

import org.ionkin.search.map.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.ionkin.search.Statistic.sqr;

public class MeanStd {

    public static void main(String... args) throws IOException {

        MeanStd col = new MeanStd();
        String[] files = new File(Util.textPath).list();
        //    for (String f : files) {
        Map<Integer, byte[]> res = col.findMeanStd(Util.textPath + files[0]); // TODO


        IntStringMap isRelat = new IntStringMap(Util.basePath + "isRelat.is");

        StringBuffer sb = new StringBuffer(500_000);
        res.forEach((k, ar) -> {
            int[] vs = Compressor.decompressVb(ar, new IntWrapper(), ar.length);
            for (int v : vs) {
                    sb.append(col.isRelat.get(k).asString());
                    sb.append(" ");
                    sb.append(col.isRelat.get(v).asString());
                    sb.append("\n");
            }
        });
        try (FileOutputStream out = new FileOutputStream(files[0])) { // TODO
            out.write(sb.toString().getBytes("UTF-8"));
        }
    }

    private final StringStringMap lemms = new StringStringMap(Util.wordLemmPath);
    private final StringIntMap siRelat = new StringIntMap(Util.basePath + "siRelat.si");
    final IntStringMap isRelat = new IntStringMap(Util.basePath + "isRelat.is");
    private final StringStringMap langPart = new StringStringMap(Util.basePath + "language-part.ssm");
    private final CompactHashMap<Integer, Integer> wordFreq = new CompactHashMap<>(new IntIntTranslator());
    private long allWords = 0;

    public MeanStd() throws IOException {
    }

    public HashMap<Integer, byte[]> findMeanStd(String absFileName) throws IOException {
        HashMap<Integer, ByteArray> wordIdWindowIdsGlob = wordIdWindowIdsGlob(absFileName);
        HashMap<Integer, byte[]> wordIdIdsMap = new HashMap<>();
        wordIdWindowIdsGlob.forEach((wordId, windowIdsWithDistance) -> {
            LightString key = isRelat.get(wordId);
            if (key.length() <= 3 && !key.isPositiveInteger()) return;
            int[] arC = VariableByte.uncompress(windowIdsWithDistance.getCopy(), new IntWrapper(), windowIdsWithDistance.size());
            IntPair[] wordIdDistance = new IntPair[arC.length / 2];
            for (int i = 0; i < arC.length; i += 2) {
                wordIdDistance[i >> 1] = new IntPair(arC[i], arC[i + 1]);
            }
            Arrays.sort(wordIdDistance, Comparator.comparing(IntPair::getI1));
            byte[] cols = findAllCollocations(wordIdDistance, wordId);
            if (cols.length != 0) {
                wordIdIdsMap.put(wordId, cols);
            }
        });
        return wordIdIdsMap;
    }


    private HashMap<Integer, ByteArray> wordIdWindowIdsGlob(String absFileName) throws IOException {
        WikiParser wikiParser = new WikiParser(absFileName);
        HashMap<Integer, ByteArray> wordIdWindowIdsGlob = new HashMap<>();
        wikiParser.getPages().forEach(p -> {
            List<LightString> list = new ArrayList<>();
            String[] words = Util.splitPattern.split(p.getContent());
            for (String word : words) {
                String normalWord = Util.normalize(word);
                if (Util.searchable(normalWord)) {
                    LightString ls = new LightString(normalWord);
                    ls = lemms.getOrDefault(ls, ls);
                    if (siRelat.containsKey(ls)) {    // TODO: bug at index
                        wordFreq.merge(siRelat.get(ls), 1, Integer::sum);
                        allWords++;
                        list.add(ls);
                    }
                }
            }
            LightString[] ar = list.toArray(new LightString[0]);
            HashMap<Integer, ByteArray> meanStd = wordIdWindowIds(ar);
            concatValues(wordIdWindowIdsGlob, meanStd);
        });
        return wordIdWindowIdsGlob;
    }

    // checked
    HashMap<Integer, ByteArray> wordIdWindowIds(LightString[] words) {
        int wind = 4;
        HashMap<Integer, ByteArray> res = new HashMap<>();
        for (int i = 0; i < words.length - 1; i++) {
            int ind = siRelat.get(words[i]);
            for (int k = 1; k < Math.min(wind + 1, words.length - i); k++) {
                int next = siRelat.get(words[i + k]);
                ByteArray ar = res.get(ind);
                if (ar == null) {
                    ar = new ByteArray();
                    ar.addVb(next);
                    ar.addVb(k);
                    res.put(ind, ar);
                } else {
                    ar.addVb(next);
                    ar.addVb(k);
                }
            }
        }
        return res;
    }

    byte[] findAllCollocations(IntPair[] wordIdDist, int wordId) {
        ByteArray res = new ByteArray();
        int i = 0;
        while (i < wordIdDist.length) {
            final int nEquals = equalsByKeyCount(wordIdDist, i);
            if (nEquals >= 40) {
                double u = Statistic.meanByValue(wordIdDist, i, i + nEquals);
                double D2 = Statistic.dispByValue(wordIdDist, i, i + nEquals);
                IntPair pair = new IntPair(wordId, wordIdDist[i].getI1());
                if (u < 2.5 && D2 < 1.5
                        && Statistic.partFilter(pair, isRelat, langPart)
                        && !checkHipotesByXi(pair, nEquals / 4)) {
                    res.addVb(wordIdDist[i].getI1());
                }
            }
            i += nEquals;
        }
        if (res.size() != 0) {
            int[] ar = VariableByte.uncompress(res.getCopy(), new IntWrapper(), res.size());
            Arrays.sort(ar); // TODO: need to sort before?
            return Compressor.compressVbWithoutMemory(ar);
        } else {
            return new byte[0];
        }
    }

    private boolean checkHipotesByXi(IntPair pair, int pairFreq) {
        long o11 = pairFreq;
        long o12 = wordFreq.get(pair.getI1()) - pairFreq;
        long o21 = wordFreq.get(pair.getI2()) - pairFreq;
        long o22 = allWords - pairFreq;
        double r = allWords * sqr(o11 * o22 - o12 * o21) / (o11 + o12) / (o11 + o21) / (o12 + o22) / (o21 + o22);
        return r < 3.841;
    }

    private static int equalsByKeyCount(IntPair[] wordIdDist, int i) {
        int nEquals = 1;
        while ((i + nEquals < wordIdDist.length) && wordIdDist[i].getI1() == wordIdDist[i + nEquals].getI1()) {
            nEquals++;
        }
        return nEquals;
    }

    // checked
    static void concatValues(HashMap<Integer, ByteArray> glob, HashMap<Integer, ByteArray> loc) {
        loc.forEach((k, v2) -> {
            ByteArray v1 = glob.get(k);
            if (v1 == null) {
                glob.put(k, v2);
            } else {
                v1.add(v2.getCopy());
            }
        });
    }
}
