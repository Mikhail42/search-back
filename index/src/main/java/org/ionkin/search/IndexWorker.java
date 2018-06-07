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

public class IndexWorker {
    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    public static void main(String... args) throws Exception {
        logger.debug("started");
        //writeIndex(Util.basePath + "testText/", Util.testIndexPath);
        writeIndex(Util.textPath, Util.indexFolder);
        joinIndex();
    }

    public static void joinIndex() throws IOException {
        logger.debug("try read tokens");
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(Util.dictionaryPath, new StringTranslator());
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
        map.write(Util.indexPath + "_0526_20");
    }

    public static void writeIndex(String inDir, String outDir) {
        String[] files = new File(inDir).list();
        ParallelFor.par(i -> {
            String file = files[i];
            WikiParser wikiParser = new WikiParser(inDir + file);

            final Map<LightString, List<Integer>> local = new HashMap<>();
            wikiParser.getPages().forEach(page -> {
                logger.trace("docId={}", page.getId());
                String articleText = page.getContent();
                Iterable<String> words = Util.splitPatternLazy.split(articleText);
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
                        list.add(page.getId());
                        local.put(w, list);
                    } else {
                        list.add(page.getId());
                    }
                });
            });

            StringBytesMap map = new StringBytesMap();
            local.forEach((str, list) -> {
                int[] ar = Ints.toArray(list);
                byte[] comp = Compressor.compressVbWithoutMemory(ar);
                //int[] comp = Compressor.compressS9WithoutMemory(ar);
                //byte[] bytes = IO.toBytes(comp);
                map.put(str, new BytesRange(comp));
            });

            map.write(outDir + file);
        }, 0, files.length);
    }

}