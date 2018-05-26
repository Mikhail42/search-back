package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.*;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

public class PositionsIndex {

    private static Logger logger = LoggerFactory.getLogger(PositionsIndex.class);

    public static void main(String... args) throws Exception {
        logger.info("start read. with wait");
        writePositionsByFileArticles();
        logger.info("positions writen");
        joinByN(25);
        logger.info("positions joined by 25");
        joinAll();
        logger.info("stop");
    }

    public static void joinAll() throws Exception {
        logger.info("joinBy10");
        logger.debug("try read fileIds");
        StringPositionsMap map1 = new StringPositionsMap(Util.basePath + "AA f");
        StringPositionsMap map2 = new StringPositionsMap(Util.basePath + "AZ f");
        logger.info("Both map read");
        System.gc();
        map2.forEach((k, v2) -> {
            CompactHashMap<Integer, byte[]> v1 = map1.get(k);
            if (v1 != null) {
                v1.putAll(v2);
                map1.put(k, v1);
            }
        });
        logger.info("Both map joined");
        map2 = null;
        System.gc();
        logger.info("try write final result");
        map1.write(Util.positionsPath);
    }

    public static void joinByN(int n) throws Exception {
        logger.info("joinByN parallel");
        logger.debug("try read fileIds");
        String[] filenames = new File(Util.positionIndexFolder).list();
        Arrays.sort(filenames);
        logger.debug("fileIds read from {}. size: {}", Util.positionIndexFolder, filenames.length);
        CompactHashSet<LightString> tokensMap =
                CompactHashSet.read(Util.basePath + "allTokens.chsls", new StringTranslator());
        LightString[] tokens = toArray(tokensMap);
        tokensMap = null;

        for (int i = 0; i < filenames.length; i += n) {
            logger.debug("i={}", i);
            final int i0 = i;
            final StringPositionsMap[] maps = new StringPositionsMap[n];
            ParallelFor.par(j -> {
                int ind = i0 + j;
                maps[j] = new StringPositionsMap(Util.positionIndexFolder + filenames[ind]);
                logger.info("maps[{}] written", j);
            }, 0, n);
            logger.info("try join");
            StringPositionsMap res = StringPositionsMap.join(tokens, maps);
            res.write(Util.basePath + filenames[i] + "MainN");
            res = null;
        }
    }

    public static LightString[] toArray(Set<LightString> set) {
        LightString[] a = new LightString[set.size()];
        int i = 0;
        for (LightString val : set) a[i++] = val;
        return a;
    }

    public static void writePositionsByFileArticles() {
        logger.debug("article iterator created");
        String[] files = new File(Util.textPath).list();
        Arrays.sort(files);
        ParallelFor.par(i -> {
            String filename = files[i];
            Iterator<Page> iterator = TextArticleIterator.articleTextIterator(Util.textPath + filename);
            Map<LightString, CompactHashMap<Integer, byte[]>> local = positions(iterator);
            StringPositionsMap compact = new StringPositionsMap();
            compact.putAll(local);
            compact.write(Util.positionIndexFolder + filename);
            logger.debug("success: {}", filename);
        }, 0, files.length);
    }

    private static Map<LightString, CompactHashMap<Integer, byte[]>> positions(
            Iterator<Page> textArticleIterator) {
        Map<LightString, CompactHashMap<Integer, byte[]>> local = new HashMap<>();

        while (textArticleIterator.hasNext()) {
            Page next = textArticleIterator.next();
            logger.trace("docId={}", next.getId());
            String articleText = next.getContent();
            Map<LightString, List<Integer>> positions = positionsAtPage(articleText);
            logger.trace("positions map is ready. size: {}", positions.size());
            final StringBytesMap compressedPositions = new StringBytesMap();
            positions.forEach((k, v) -> {
                int[] ar = Ints.toArray(v);
                byte[] comp = Compressor.compressVbWithoutMemory(ar);
                compressedPositions.put(k, comp);
            });
            logger.trace("compact map created. size: {}", positions.size());
            positions = null;
            compressedPositions.forEach((k, v) -> {
                CompactHashMap<Integer, byte[]> map = local.get(k);
                if (map != null) {
                    map.put(next.getId(), v);
                } else {
                    map = new CompactHashMap<>(new IntBytesTranslator());
                    map.put(next.getId(), v);
                    local.put(k, map);
                }
            });
            logger.trace("compact map written to local map");
        }

        return local;
    }

    private static Map<LightString, List<Integer>> positionsAtPage(String articleText) {
        Map<LightString, List<Integer>> res = new HashMap<>();
        Matcher wordMatcher = Util.wordPattern.matcher(articleText);
        Matcher splitMatcher = Util.splitPattern.matcher(articleText);
        int currentIndex = 0;
        while (currentIndex < articleText.length()) {
            int start = indexOf(wordMatcher, currentIndex);
            if (start == -1) break;
            int end = indexOf(splitMatcher, start);
            if (end == -1) end = articleText.length();
            currentIndex = end;

            String word = articleText.substring(start, end);
            String normalWord = Util.normalize(word);
            if (Util.searchable(normalWord)) {
                LightString lightString = new LightString(normalWord);
                List<Integer> cur = res.get(lightString);
                if (cur != null) {
                    cur.add(start);
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(start);
                    res.put(lightString, list);
                }
            }
        }
        return res;
    }

    private static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
