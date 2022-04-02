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
import java.util.function.Function;

public class Indexer {
    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    public static void main(String... args) {
        logger.info("start create inverse index");
        try {
            File indexDir = new File(Util.indexFolder);
            if (!indexDir.exists()) indexDir.mkdir();
            if (indexDir.isDirectory() && indexDir.list().length == 0) {
                writeIndex();
                joinIndex();
                buildTitleIndex();
            }
            logger.info("inverse index creation finished");
        } catch (Exception exc) {
            logger.error("Can't create or write index", exc);
            exc.printStackTrace();
        }
    }

    public static void joinIndex() throws IOException {
        logger.debug("try read tokens");
        final LightString[] tokens = TokensStore.getTokens();

        String[] files = new File(Util.indexFolder).list();
        Arrays.sort(files);
        StringBytesMap[] maps = new StringBytesMap[files.length];
        ParallelFor.par(i -> {
            maps[i] = new StringBytesMap(Util.indexFolder + files[i]);
        }, 0, files.length);

        int by = files.length / Util.threadPoolSize;
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

    public static void writeIndex() {
        File[] dirs = Util.textDirs();
        ParallelFor.par(i -> {
            File dir = dirs[i];
            StringBytesMap map = buildIndex(dir, p -> p.getContent());
            map.write(Util.indexFolder + dir.getName());
        }, 0, dirs.length);
    }

    private static void buildTitleIndex() throws IOException {
        File[] dirs = Util.textDirs();
        StringBytesMap[] sbms = new StringBytesMap[dirs.length];
        ParallelFor.par(i -> {
            File dir = dirs[i];
            sbms[i] = buildIndex(dir, p -> p.getTitle());
        }, 0, dirs.length);
        LightString[] tokens = TokensStore.getTokens();
        StringBytesMap joined = StringBytesMap.join(tokens, sbms);
        joined.write(Util.titleIndexPath);
    }

    private static StringBytesMap buildIndex(File wikiexractorSubDir, Function<Page, String> getContent) throws IOException {
        String[] files = wikiexractorSubDir.list();
        Arrays.sort(files);
        final HashMap<LightString, List<Integer>> wordToPageIds = new HashMap<>();
        for (String file : files) {
            WikiParser wikiParser = new WikiParser(wikiexractorSubDir.getAbsolutePath() + "/" + file);
            wikiParser.getPages().forEach(page -> {
                logger.trace("docId={}", page.getId());
                Set<LightString> pageWords = extractWords(getContent.apply(page));
                addWords(wordToPageIds, pageWords, page.getId());
            });
        }
        return compressMap(wordToPageIds);
    }

    private static Set<LightString> extractWords(String pageContent) {
        Iterable<String> words = Util.splitPatternLazy.split(pageContent);
        CompactHashSet<LightString> pageWords = new CompactHashSet<>(new StringTranslator());
        words.forEach(word -> {
            String normalWord = Util.normalize(word);
            // it is not good for title index, but without it it will be not so easy to create
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