package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.IntBytesTranslator;
import org.ionkin.search.map.StringBytesTranslator;
import org.ionkin.search.map.StringPositionsMapTranslator;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionsIndex {

    private static final String pathToPos = "/home/mikhail/pos100000/";
    private static final String pathToPos10 = "/home/mikhail/pos1mil/";

    private static Logger logger = LoggerFactory.getLogger(PositionsIndex.class);

    public static void main(String... args) throws Exception {
        logger.info("start read");
        //writePositionsBy100000Articles();
        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map = positionIndex();
        System.gc();
        map.wait(20 * 1000);
        logger.info("stop. size = {}", map.size());
    }

    public static CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> positionIndex() throws IOException {
        return CompactHashMap.read("/home/mikhail/pos1mil/joinAll", new StringPositionsMapTranslator());
    }

    private static final Pattern splitPattern = Pattern.compile("[^\\p{L}\\p{N}\u0301-]+");
    private static final Pattern wordPattern = Pattern.compile("[\\p{L}\\p{N}\u0301-]+");

    public static void joinAll() throws Exception {
        logger.info("joinBy10");
        logger.debug("try read fileIds");
        byte[] map1AsBytes = IO.read("/home/mikhail/pos1mil/0");
        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map1 =
                CompactHashMap.deserialize(map1AsBytes, new StringPositionsMapTranslator());
        map1AsBytes = null;
        System.gc();
        byte[] map2AsBytes = IO.read("/home/mikhail/pos1mil/join2");
        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map2 =
                CompactHashMap.deserialize(map2AsBytes, new StringPositionsMapTranslator());
        map2AsBytes = null;
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
        map1.write("/home/mikhail/pos1mil/joinAll");
    }

    public static void joinBy5() throws Exception {
        logger.info("joinBy10");
        logger.debug("try read fileIds");
        int[] fileIds = FileWorker.getFileNamesMathesDigits(pathToPos);
        Arrays.sort(fileIds);
        logger.debug("fileIds read from {}. size: {}", pathToPos, fileIds.length);
        String filename = PositionsIndex.class.getClassLoader().getResource("allTokens.chsls").getFile();
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(filename, new StringTranslator());
        LightString[] tokens = toArray(tokensMap);
        tokensMap = null;

        for (int i=0; i<fileIds.length; i+=5) {
            logger.debug("i={}", i);
            CompactHashMap<LightString, CompactHashMap<Integer, byte[]>>[] maps = new CompactHashMap[5];
            for (int j=0; j<5; j++) {
                int ind = i+j;
                byte[] mapAsBytes = IO.read(pathToPos + fileIds[ind]);
                maps[j] = CompactHashMap.deserialize(mapAsBytes, new StringPositionsMapTranslator());
            }
            logger.info("try join");
            CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> res = CompactHashMap.join(tokens, maps);
            maps = null;
            byte[] resAsBytes = res.serialize();
            res = null;
            IO.write(resAsBytes, pathToPos10 + fileIds[i]);
            resAsBytes = null;
        }
    }

    public static LightString[] toArray(Set<LightString> set) {
        LightString[] a = new LightString[set.size()];
        int i = 0;
        for (LightString val : set) a[i++] = val;
        return a;
    }

    public static void writePositionsBy100000Articles() throws Exception {
        TextArticleIterator textArticleIterator = new TextArticleIterator();
        logger.debug("article iteratoc created");
        Iterator<Page> articleIterator = textArticleIterator.articleTextIterator();
        int step = 100000;
        int until = step;

        for (int from = 0; articleIterator.hasNext(); from += step, until += step) {
            Map<LightString, CompactHashMap<Integer, byte[]>> local =
                    positionsAtRangeOf100000Pages(articleIterator, from, until);
            if (articleIterator.hasNext()) {
                logger.info("has next. from: {}, until: {}", from, until);
            } else {
                logger.warn("hasn't next. from: {}, until: {}", from, until);
            }
            CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> compact =
                    new CompactHashMap<>(new StringPositionsMapTranslator());
            compact.putAll(local);
            byte[] map = compact.serialize();
            IO.write(map, "/home/mikhail/pos100000/" + from);
            logger.debug("success");
        }
    }

    public static Map<LightString, CompactHashMap<Integer, byte[]>> positionsAtRangeOf100000Pages(
            Iterator<Page> textArticleIterator, int from, int until) {
        Map<LightString, CompactHashMap<Integer, byte[]>> local = new HashMap<>();

        for (int i = from; i < until & textArticleIterator.hasNext(); i++) {
            Page next = textArticleIterator.next();
            logger.trace("docId={}", next.getId());
            String articleText = next.getContent();
            Map<LightString, List<Integer>> positions = positionsAtPage(articleText);
            logger.trace("positions map is ready. size: {}", positions.size());
            final CompactHashMap<LightString, byte[]> compressedPositions = new CompactHashMap<>(new StringBytesTranslator());
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
            logger.trace("compact map writen to local map");
        }

        return local;
    }

    public static Map<LightString, List<Integer>> positionsAtPage(String articleText) {
        Map<LightString, List<Integer>> res = new HashMap<>();
        Matcher wordMatcher = wordPattern.matcher(articleText);
        Matcher splitMatcher = splitPattern.matcher(articleText);
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

    private Map<LightString, Integer> createWordCountMap(String text) {
        Map<LightString, Integer> wordCountMap = new HashMap<>();
        String[] words = Util.splitJavaPattern.split(text);
        for (String word : words) {
            String norm = Util.normalize(word);
            if (Util.searchable(norm)) {
                LightString lightString = new LightString(norm);

            }
        }
        return wordCountMap;
    }

    public static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
