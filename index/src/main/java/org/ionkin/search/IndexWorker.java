package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringBytesTranslator;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class IndexWorker {
    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    public static void main(String... args) throws Exception {
        logger.debug("started. 0.5 15");
        joinIndex();
    }

    public static void joinIndex() throws IOException {
        int i0 = 0;
        int n = 15;
        CompactHashMap<LightString, byte[]>[] maps = new CompactHashMap[n];
        int step = 100_000;
        for (int i = i0; i < i0 + n; i++) {
            String filename = "/home/mikhail/index100000/" + i * step;
            logger.debug("try read {}", filename);
            maps[i] = CompactHashMap.read(filename, new StringBytesTranslator());
        }
        logger.debug("try read tokens");
        String tokensFilename = PositionsIndex.class.getClassLoader().getResource("allTokens.chsls").getFile();
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(tokensFilename, new StringTranslator());
        LightString[] tokens = PositionsIndex.toArray(tokensMap);
        tokensMap = null;
        logger.debug("try join all");
        CompactHashMap<LightString, byte[]> map = CompactHashMap.joinStringBytesMap(tokens, maps);
        tokens = null;
        logger.debug("try write all");
        map.write("/home/mikhail/allIndex.lsbytes");
    }

    public static void writeIndexBy100k() throws IOException {
        Iterator<Page> textArticleIterator = new TextArticleIterator().articleTextIterator();
        final int step = 100_000;
        for (int i = 0; i < 1_500_000; i += step) {
            logger.info("i=" + i);
            try {
                CompactHashMap<LightString, byte[]> res = indexOf100000Pages(textArticleIterator, i, i + step);
                res.write("/home/mikhail/index100000/" + i);
            } catch (Exception e) {
                logger.error("error: i=" + i, e);
            }
        }
    }

    public static CompactHashMap<LightString, byte[]> indexOf100000Pages(
            Iterator<Page> textArticleIterator, int from, int until) {
        final Map<LightString, List<Integer>> local = new HashMap<>();

        for (int i = from; i < until & textArticleIterator.hasNext(); i++) {
            Page next = textArticleIterator.next();
            logger.trace("docId={}", next.getId());
            String articleText = next.getContent();
            Iterable<String> words = Util.splitPattern.split(articleText);
            CompactHashSet<LightString> pageWords = new CompactHashSet<>(new StringTranslator());
            words.forEach(word -> {
                String normalWord = Util.normalize(word);
                if (Util.searchable(normalWord)) {
                    pageWords.add(new LightString(normalWord));
                }
            });
            pageWords.forEach(w -> {
                List<Integer> list = local.get(w);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(next.getId());
                    local.put(w, list);
                } else {
                    list.add(next.getId());
                }
            });
        }

        CompactHashMap<LightString, byte[]> map = new CompactHashMap<>(new StringBytesTranslator());
        local.forEach((str, list) -> {
            int[] ar = Ints.toArray(list);
            byte[] bytes = Compressor.compressVbWithoutMemory(ar);
            map.put(str, bytes);
        });

        return map;
    }
}