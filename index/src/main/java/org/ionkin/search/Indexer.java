package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.map.StringBytesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Indexer {
    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    public static void main(String... args) {
        init();
    }

    public static void init() {
        logger.info("start create inverse index if not exist");
        try {
            File indexDir = new File(Util.indexFolder);
            if (!indexDir.exists()) indexDir.mkdir();
            if (indexDir.list().length == 0) {
                logger.info("start create inverse index");
                writeIndexes();
                joinIndex();
                logger.info("inverse index creation finished");
            } else {
                logger.info("inverse index already exists");
            }
        } catch (Exception exc) {
            logger.error("Can't create or write index", exc);
            exc.printStackTrace();
        }
    }

    public static StringBytesMap readIndex() throws IOException {
        return new StringBytesMap(Util.indexPath);
    }

    public static StringBytesMap readTitleIndex() throws IOException {
        return new StringBytesMap(Util.titleIndexPath);
    }

    private static void joinIndex() throws IOException {
        logger.debug("try read tokens");
        final LightString[] tokens = TokensStore.getTokens();

        String[] files = new File(Util.indexFolder).list();
        Arrays.sort(files);
        StringBytesMap[] maps = new StringBytesMap[files.length];
        for (int i = 0; i < files.length; i++) {
            maps[i] = new StringBytesMap(Util.indexFolder + files[i]);
        }

        int by = files.length / Util.threadPoolSize;
        int n = files.length / by + ((files.length % by == 0) ? 0 : 1);
        logger.info("n = {}", n);
        StringBytesMap[] mapsBy = new StringBytesMap[n];
        for (int k = 0; k < n; k++) {
            StringBytesMap[] mapsToJoin = Arrays.copyOfRange(maps, k * by, Math.min((k + 1) * by, maps.length));
            mapsBy[k] = StringBytesMap.join(tokens, mapsToJoin);
            for (int i = k * by; i < Math.min((k + 1) * by, maps.length); i++) maps[i] = null;
        }
        System.gc();

        logger.debug("try join all");
        StringBytesMap map = StringBytesMap.join(tokens, mapsBy);

        logger.debug("try write all");
        map.write(Util.indexPath);
    }

    private static void writeIndexes() throws IOException {
        File[] dirs = Util.textDirs();
        StringBytesMap[] titleIndexForDirs = new StringBytesMap[dirs.length];

        for (int i=0; i<dirs.length; i++) {
            File dir = dirs[i];
            titleIndexForDirs[i] = buildIndex(dir, p -> p.getTitle());
            StringBytesMap indexForDir = buildIndex(dir, p -> p.getContent());
            indexForDir.write(Util.indexFolder + dir.getName());
        }

        LightString[] tokens = TokensStore.getTokens();
        StringBytesMap joinedTitleIndex = StringBytesMap.join(tokens, titleIndexForDirs);
        joinedTitleIndex.write(Util.titleIndexPath);
    }

    private static StringBytesMap buildIndex(File wikiexractorSubDir, Function<Page, String> getContent) throws IOException {
        String[] files = wikiexractorSubDir.list();
        Arrays.sort(files);
        final HashMap<LightString, List<Integer>> wordToPageIds = new HashMap<>();
        for (String file : files) {
            WikiParser wikiParser = new WikiParser(wikiexractorSubDir.getAbsolutePath() + "/" + file);
            wikiParser.getPages().forEach(page -> {
                logger.trace("docId={}", page.getId());
                Set<LightString> pageWords = Util.uniqueWords(getContent.apply(page));
                addWords(wordToPageIds, pageWords, page.getId());
            });
        }
        return compressMap(wordToPageIds);
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