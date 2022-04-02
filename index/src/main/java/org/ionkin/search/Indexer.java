package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.StringBytesMap;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Indexer {
    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    public static void main(String... args) throws Exception {
        logger.debug("started");
        writeIndex(Util.textPath, Util.indexFolder);
        joinIndex();
        //buildTitleIndex(Util.textPath);
    }

    public static void joinIndex() throws IOException {
        logger.debug("try read tokens");
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(Util.tokensPath, new StringTranslator());
        final LightString[] tokens = Util.toArray(tokensMap);
        tokensMap = null;

        String[] files = new File(Util.indexFolder).list();
        StringBytesMap[] maps = new StringBytesMap[files.length];
        ParallelFor.par(i -> {
            maps[i] = new StringBytesMap(Util.indexFolder + files[i]);
        }, 0, files.length);

        int by = files.length / 4;  // 4 thread
        int n = files.length / by + ((files.length % by == 0) ? 0 : 1);
        logger.info("n = {}", n);
        StringBytesMap[] mapsBy = new StringBytesMap[n];
        ParallelFor.par(k -> {
            StringBytesMap[] mapsToJoin = Arrays.copyOfRange(maps, k * by, Math.min((k + 1) * by, maps.length));
            mapsBy[k] = StringBytesMap.join(tokens, mapsToJoin);
            for (int i = k * by; i < Math.min((k + 1) * by, maps.length); i++) maps[i] = null;
        }, 0, n);
        System.gc();
        logger.debug("try join all");
        StringBytesMap map = StringBytesMap.join(tokens, mapsBy);

        logger.debug("try write all");
        map.write(Util.indexPath);
    }

    public static void writeIndex(String inDir, String outDir) {
        String[] files = new File(inDir).list();
        ParallelFor.par(i -> {
            String file = files[i];
            StringBytesMap map = buildIndex(inDir + file);
            map.write(outDir + file);
        }, 0, files.length);
    }

    private static StringBytesMap buildIndex(String absFileName) throws IOException {
        WikiParser wikiParser = new WikiParser(absFileName);

        final HashMap<LightString, List<Integer>> local = new HashMap<>();
        wikiParser.getPages().forEach(page -> {
            logger.trace("docId={}", page.getId());
            Set<LightString> pageWords = extractWords(page.getContent());
            addWords(local, pageWords, page.getId());
        });

        return compressMap(local);
    }

    private static void buildTitleIndex(String inDir) throws IOException {
        String[] files = new File(inDir).list();
        StringBytesMap global = new StringBytesMap();
        ParallelFor.par(i -> {
            logger.info("filename: {}", files[i]);
            Map<Integer, Set<LightString>> idTitleMap = extractTitles(inDir + files[i]);
            Map<LightString, byte[]> from = from(idTitleMap);
            synchronized (global) {
                join(global, from);
            }
        }, 0, files.length);
        global.write(Util.basePath + "titleindexNoLemm.sbm");
    }

    private static void join(StringBytesMap global, Map<LightString, byte[]> local) {
        local.forEach((w, locInd) -> {
            BytesRange range = global.get(w);
            if (range != null && range.length() != 0) {
                int[] idsGlob = Compressor.decompressVb(range);
                int[] idsLoc = Compressor.decompressVb(locInd);
                IntArray ar = new IntArray(idsGlob.length + idsLoc.length);
                ar.add(idsGlob);
                ar.add(idsLoc);
                int[] res = ar.getAll();
                Arrays.sort(res);
                byte[] joined = Compressor.compressVbWithoutMemory(res);
                global.put(w, new BytesRange(joined));
            } else {
                global.put(w, new BytesRange(locInd));
            }
        });
    }

    private static Map<LightString, byte[]> from(Map<Integer, Set<LightString>> rightIndex) {
        Map<LightString, byte[]> resMap = new HashMap<>();
        rightIndex.forEach((k, v) -> {
            v.forEach(w -> {
                byte[] inds = resMap.get(w);
                if (inds != null) {
                    int[] curInds = Compressor.decompressVb(inds);
                    IntArray ar = new IntArray(curInds.length + 1);
                    ar.add(curInds);
                    ar.add(k);
                    int[] res = ar.getAll();
                    Arrays.sort(res);
                    resMap.put(w, Compressor.compressVbWithoutMemory(res));
                } else {
                    resMap.put(w, VariableByte.compress(k));
                }
            });
        });
        return resMap;
    }

    private static Map<Integer, Set<LightString>> extractTitles(String absFileName) throws IOException {
        WikiParser wikiParser = new WikiParser(absFileName);
        Map<Integer, Set<LightString>> res = new HashMap<>();
        wikiParser.getPages().forEach(page -> {
            String[] ar = Util.splitPattern.split(page.getTitle());
            Set<LightString> set = new HashSet<>();
            for (String s : ar) {
                String s2 = Util.normalize(s);
                LightString word = new LightString(s2);
                set.add(word);
            }
            res.put(page.getId(), set);
        });
        return res;
    }

    private static Set<LightString> extractWords(String pageContent) {
        Iterable<String> words = Util.splitPatternLazy.split(pageContent);
        CompactHashSet<LightString> pageWords = new CompactHashSet<>(new StringTranslator());
        words.forEach(word -> {
            String normalWord = Util.normalize(word);
            if (Util.searchable(normalWord)) {
                pageWords.add(new LightString(normalWord));
            }
        });
        return pageWords;
    }

    private static void addWords(Map<LightString, List<Integer>> map, Set<LightString> pageWords, int docId) {
        pageWords.forEach(w -> {
            List<Integer> list = map.get(w);
            if (list == null) {
                list = new ArrayList<>();
                list.add(docId);
                map.put(w, list);
            } else {
                list.add(docId);
            }
        });
    }

    private static StringBytesMap compressMap(HashMap<LightString, List<Integer>> map) {
        StringBytesMap compressed = new StringBytesMap();
        map.forEach((str, list) -> {
            int[] ar = Ints.toArray(list);
            byte[] comp = Compressor.compressVbWithoutMemory(ar);
            compressed.put(str, new BytesRange(comp));
        });
        return compressed;
    }
}