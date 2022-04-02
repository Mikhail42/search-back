package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.*;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

public class PositionsIndex {

    private static final Logger logger = LoggerFactory.getLogger(PositionsIndex.class);

    public static void main(String... args) throws Exception {
        try {
            logger.info("start read. with wait");
            //testJoin();
            writePositionsByFileArticles(Util.positionIndexFolder);
            logger.info("positions written");
            joinByN(25);
            logger.info("positions joined by 25");
            joinAll();
            logger.info("stop");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void joinAll() throws Exception {
        logger.info("joinBy10");
        logger.debug("try read fileIds");
        StringPositionsMap map1 = new StringPositionsMap(Util.basePath + "AA fMainN");
        StringPositionsMap map2 = new StringPositionsMap(Util.basePath + "AZ fMainN");
        logger.info("Both map read");
        System.gc();
        map2.forEach((k, v2) -> {
            IntBytesMap v1 = map1.get(k);
            if (v1 != null) {
                v1.putAll(v2);
                map1.put(k, v1);
            }
        });
        logger.info("Both map joined");
        map2 = null;
        System.gc();
        logger.info("try write final result");
        map1.write(Util.positionsPath + "NWord");
    }

    public static void joinByN(int n) throws Exception {
        logger.info("joinByN parallel");
        logger.debug("try read fileIds");
        String[] filenames = new File(Util.positionIndexFolder).list();
        Arrays.sort(filenames);
        logger.debug("fileIds read from {}. size: {}", Util.positionIndexFolder, filenames.length);
        LightString[] tokens = TokensStore.getTokens();

        for (int i = 0; i < filenames.length; i += n) {
            logger.debug("i={}", i);
            final int i0 = i;
            final StringPositionsMap[] maps = new StringPositionsMap[n];
            ParallelFor.par(j -> {
                int ind = i0 + j;
                maps[j] = new StringPositionsMap(Util.positionIndexFolder + filenames[ind]);
                logger.info("maps[{}] read", j);
            }, 0, n);
            logger.info("try join");
            StringPositionsMap res = StringPositionsMap.join(tokens, maps);
            res.write(Util.basePath + filenames[i] + "MainN");
            res = null;
        }
    }

    public static void writePositionsByFileArticles(String outDir) throws IOException {
        logger.debug("article iterator created");
        LightString[] tokens = TokensStore.getTokens();
        for (File wikiExtractorSubDir : Util.textDirs()) {
            String[] files = wikiExtractorSubDir.list();
            Arrays.sort(files);
            StringPositionsMap[] spms = new StringPositionsMap[files.length];
            ParallelFor.par(i -> {
                String fullFileName = wikiExtractorSubDir.getAbsolutePath() + "/" + files[i];
                Iterator<Page> iterator = TextArticleIterator.articleTextIterator(fullFileName);
                Map<LightString, IntBytesMap> local = positions(iterator);
                spms[i] = new StringPositionsMap();
                spms[i].putAll(local);
            }, 0, files.length);
            StringPositionsMap resultMap = StringPositionsMap.join(tokens, spms);
            String filename = wikiExtractorSubDir.getName() + ".spm";
            logger.debug("success: {}", filename);
            resultMap.write(outDir + filename);
        }
    }

    static Map<LightString, IntBytesMap> positions(Iterator<Page> textArticleIterator) {
        Map<LightString, IntBytesMap> local = new HashMap<>();

        while (textArticleIterator.hasNext()) {
            Page next = textArticleIterator.next();
            logger.trace("docId={}", next.getId());
            String articleText = next.getContent();
            Map<LightString, List<Integer>> positions = positionsAtPage(articleText);
            logger.trace("positions map is ready. size: {}", positions.size());
            positions.forEach((k, v) -> {
                int[] ar = Ints.toArray(v);
                byte[] compAsBytes = Compressor.compressVbWithoutMemory(ar);
                IntBytesMap intBytesMap = local.get(k);
                if (intBytesMap == null) {
                    intBytesMap = new IntBytesMap();
                }
                intBytesMap.put(next.getId(), new BytesRange(compAsBytes));
                local.put(k, intBytesMap);
            });
            logger.trace("compact map created. size: {}", positions.size());
            positions = null;
            logger.trace("compact map written to local map");
        }

        return local;
    }

    static Map<LightString, List<Integer>> positionsAtPage(String articleText) {
        Map<LightString, List<Integer>> res = new HashMap<>();
        Matcher wordMatcher = Util.wordPattern.matcher(articleText);
        Matcher splitMatcher = Util.splitPattern.matcher(articleText);
        int currentIndex = 0;
        int nWord = 0;
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
                    cur.add(nWord);
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(nWord);
                    res.put(lightString, list);
                }
                nWord++;
            }
        }
        return res;
    }

    private static int indexOf(Matcher matcher, int fromIndex) {
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }
}
