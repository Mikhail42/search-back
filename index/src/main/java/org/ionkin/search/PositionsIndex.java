package org.ionkin.search;

import org.ionkin.search.map.*;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ionkin.search.Util.basePath;

public class PositionsIndex {

    private static final String pathToPos = "/home/mikhail/pos100000/";
    private static final String pathToPos10 = "/home/mikhail/pos1mil/";

    private static Logger logger = LoggerFactory.getLogger(PositionsIndex.class);

    public static void main(String... args) throws Exception {
        logger.info("start");
        writePositionsBy100000Articles();
        //joinBy10();
        logger.info("stop");
    }

    private static final Pattern splitPattern = Pattern.compile("[^\\p{L}\\p{N}\u0301-]+");
    private static final Pattern wordPattern = Pattern.compile("[\\p{L}\\p{N}\u0301-]+");

    public static void joinBy10() throws Exception {
        logger.info("joinBy10");
        logger.debug("try read fileIds");
        int[] fileIds = FileWorker.getFileNamesMathesDigits(pathToPos);
        Arrays.sort(fileIds);
        logger.debug("fileIds read from {}. size: {}", pathToPos, fileIds.length);
        String filename = PositionsIndex.class.getClassLoader().getResource("allTokens.chsls").getFile();
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(filename, new StringTranslator());
        LightString[] tokens = toArray(tokensMap);
        tokensMap = null;

        for (int i=0; i<fileIds.length; i+=10) {
            logger.debug("i={}", i);
            new HashMap<String, String>();
            int k = Math.min(10, 42 - i);
            CompactHashMap<LightString, CompactHashMap<Integer, byte[]>>[] maps = new CompactHashMap[k];
            for (int j=0; j<k; j++) {
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
        int[] documentIds = readDocIds();
        logger.debug("document ids size: {}", documentIds.length);
        TextArticleIterator textArticleIterator = new TextArticleIterator();
        // TODO
        int index = textArticleIterator.getFirstDocIdIndexByDocId(4192476);
        logger.debug("start index: {}", index);
        Iterator<Page> articleIterator = textArticleIterator.articleTextIterator(index);

        final int step = 100000;
        for (int i = 0; i < documentIds.length; i += step) {
            // TODO
            if (i < 23 * step) continue;
            int from = i;
            int until = Math.min(documentIds.length, i + step);
            logger.debug("from: {}, until: {}", from, until);
            Map<LightString, CompactHashMap<Integer, byte[]>> local =
                    positionsAtRangeOf100000Pages(articleIterator, documentIds, from, until);
            CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> compact =
                    new CompactHashMap<>(new StringPositionsMapTranslator());
            compact.putAll(local);
            byte[] map = compact.serialize();
            IO.write(map, "/home/mikhail/pos100000/" + documentIds[from]);
            logger.debug("success");
        }
    }

    private static int[] readDocIds() throws IOException {
        byte[] bytes = IO.read(basePath + "ids/allDocumentId");
        bytes = Compressor.decompressAndSumInts(bytes);
        int[] documentIds = IO.readArrayInt(bytes, 0, bytes.length / 4);
        Arrays.sort(documentIds);
        if (documentIds[0] != 7 || documentIds[1] != 11) {
            throw new InvalidObjectException("First document id must be 7, and second must be 11");
        }
        logger.debug("documents size: {}, last docId is {}", documentIds.length, documentIds[documentIds.length - 1]);
        return documentIds;
    }

    public static Map<LightString, CompactHashMap<Integer, byte[]>> positionsAtRangeOf100000Pages(
            Iterator<Page> textArticleIterator, int[] documentIds, int from, int until) {
        Map<LightString, CompactHashMap<Integer, byte[]>> local = new HashMap<>();

        for (int i = from; i < until & textArticleIterator.hasNext(); i++) {
            int docId = documentIds[i];
            logger.debug("docID: {}", docId);
            String articleText = textArticleIterator.next().getContent();
            Map<LightString, List<Integer>> positions = positionsAtPage(articleText);
            final CompactHashMap<LightString, byte[]> compressedPositions = new CompactHashMap<>(new StringBytesTranslator());
            positions.forEach((k, v) -> {
                int[] ar = v.stream().mapToInt(intV -> intV).toArray();
                byte[] comp = Compressor.diffAndCompressInts(ar);
                compressedPositions.put(k, comp);
            });
            positions = null;
            compressedPositions.forEach((k, v) -> {
                CompactHashMap<Integer, byte[]> map = local.get(k);
                if (map != null) {
                    map.put(docId, v);
                } else {
                    map = new CompactHashMap<>(new IntBytesTranslator());
                    map.put(docId, v);
                    local.put(k, map);
                }
            });
        }

        return local;
    }

    public static Map<LightString, List<Integer>> positionsAtPage(String articleText) {
        Map<LightString, List<Integer>> res = new HashMap<>();
        logger.debug("article text length: {}", articleText.length());
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
