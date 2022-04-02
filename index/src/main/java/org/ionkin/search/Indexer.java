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

    public static void main(String... args) {
        logger.info("started");
        try {
            File indexDir = new File(Util.indexFolder);
            if (indexDir.exists() && indexDir.isDirectory() && indexDir.list().length == 0) {
                writeIndex();
                joinIndex();
                buildTitleIndex(Util.textPath);
            }
        } catch (Exception exc) {
            logger.error("Can't write index", exc);
            exc.printStackTrace();
        }
    }

    public static void joinIndex() throws IOException {
        logger.debug("try read tokens");
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(Util.tokensPath, new StringTranslator());
        final LightString[] tokens = Util.toArray(tokensMap);
        tokensMap = null;

        String[] files = new File(Util.indexFolder).list();
        Arrays.sort(files);
        StringBytesMap[] maps = new StringBytesMap[files.length];
        ParallelFor.par(i -> {
            maps[i] = new StringBytesMap(Util.indexFolder + files[i]);
        }, 0, files.length);

        logger.debug("try join all");
        StringBytesMap map = StringBytesMap.join(tokens, maps);

        logger.debug("try write all");
        map.write(Util.indexPath);
    }

    public static void writeIndex() {
        File[] dirs = Util.textDirs;
        ParallelFor.par(i -> {
            File dir = dirs[i];
            StringBytesMap map = buildIndex(dir);
            map.write(Util.indexFolder + dir.getName());
        }, 0, dirs.length);
    }

    private static StringBytesMap buildIndex(File wikiexractorSubDir) throws IOException {
        String[] files = wikiexractorSubDir.list();
        Arrays.sort(files);
        final HashMap<LightString, List<Integer>> wordToPageIds = new HashMap<>();
        for (String file : files) {
            WikiParser wikiParser = new WikiParser(wikiexractorSubDir.getAbsolutePath() + "/" + file);
            wikiParser.getPages().forEach(page -> {
                logger.trace("docId={}", page.getId());
                Set<LightString> pageWords = extractWords(page.getContent());
                addWords(wordToPageIds, pageWords, page.getId());
            });
        }
        return compressMap(wordToPageIds);
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
        global.write(Util.titleIndexPath);
    }

    private static void join(StringBytesMap global, Map<LightString, byte[]> local) {
        local.forEach((word, locInd) -> {
            BytesRange range = global.get(word);
            if (range != null && range.length() != 0) {
                int[] idsGlob = Compressor.decompressVb(range);
                int[] idsLoc = Compressor.decompressVb(locInd);
                IntArray ar = new IntArray(idsGlob.length + idsLoc.length);
                ar.add(idsGlob);
                ar.add(idsLoc);
                int[] res = ar.getAll();
                Arrays.sort(res);
                byte[] joined = Compressor.compressVbWithoutMemory(res);
                global.put(word, new BytesRange(joined));
            } else {
                global.put(word, new BytesRange(locInd));
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

    private static void addWords(Map<LightString, List<Integer>> wordToPageIds, Set<LightString> pageWords, int pageId) {
        pageWords.forEach(word -> {
            List<Integer> pageIds = wordToPageIds.get(word);
            if (pageIds == null) {
                pageIds = new ArrayList<>();
                pageIds.add(pageId);
                wordToPageIds.put(word, pageIds);
            } else {
                pageIds.add(pageId);
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