package org.ionkin.search;

import com.google.common.primitives.Ints;
import org.ionkin.search.model.IntIntPair;
import org.apache.commons.io.FileUtils;
import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.IntIntIntTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TextArticleIterator {
    private static final Logger logger = LoggerFactory.getLogger(TextArticleIterator.class);

    private static Map<Integer, String> firstdocidFilenameMap;
    private static int[] firstDocIds;
    private static CompactHashMap<Integer, IntIntPair> docidPositionMap;

    private static boolean wasInit = false;

    static {
        init();
    }

    static void init() {
        if (wasInit) return;
        try {
            if (!new File(Util.firstDocidFilenamePath).exists()) {
                writeFirstDocidFilenameMap();
            }
            firstdocidFilenameMap = readFirstDocidFilenameMap();
            firstDocIds = Ints.toArray(firstdocidFilenameMap.keySet());
            Arrays.sort(firstDocIds);

            docidPositionMap = new CompactHashMap<>(new IntIntIntTranslator(), Util.docidPosPath);
        } catch (Exception e) {
            logger.error("Can't read firstdocidFilenameMap", e);
        }
    }

    public static Page readPage(int docId) throws IOException {
        int index = getFirstDocIdIndexByDocId(docId);
        int firstDocId = firstDocIds[index];
        logger.debug("first doc for article with id={} is '{}'", docId, firstDocId);
        String filename = Util.textPath + firstdocidFilenameMap.get(firstDocId);
        IntIntPair startLength = docidPositionMap.get(docId);
        return WikiParser.parsePage(filename, startLength.first(), startLength.second());
    }

    public static int getFirstDocIdIndexByDocId(int docId) {
        int index = Arrays.binarySearch(firstDocIds, docId);
        if (index < 0) {
            // articleIds[insertedPoint] > articleId || insertedPoint > lengt
            // insertedPoint = -(index + 1). lowerBound == -(index + 1) - 1 == -index-2
            index = -index - 2;
        }
        return index;
    }

    public static Iterator<Page> articleTextIterator() {
        return new Iterator<Page>() {
            private int firstDocIdIndex = 0;
            private Iterator<Page> pageIterator;

            {
                initPageIterator();
            }

            private void initPageIterator() {
                logger.debug("init iterator with index: {}, firstDocId: {}", firstDocIdIndex, firstDocIds[firstDocIdIndex]);
                String filename = Util.textPath + firstdocidFilenameMap.get(firstDocIds[firstDocIdIndex]);
                firstDocIdIndex++;
                try {
                    pageIterator = articleTextIterator(filename);
                } catch (IOException ioe) {
                    logger.error("Can't read from {}", filename);
                }
            }

            @Override
            public boolean hasNext() {
                return pageIterator.hasNext() || firstDocIdIndex < firstDocIds.length - 1;
            }

            @Override
            public Page next() {
                if (!pageIterator.hasNext()) {
                    initPageIterator();
                }
                return pageIterator.next();
            }
        };
    }

    public static Iterator<Page> articleTextIterator(String filename) throws IOException {
        logger.debug("init iterator with file: {}", filename);
        WikiParser parser = new WikiParser(filename);
        List<Page> batch = parser.getPages();
        return batch.iterator();
    }

    static void writePositions() throws IOException {
        CompactHashMap<Integer, IntIntPair> docidPositionMap =
                new CompactHashMap<>(new IntIntIntTranslator());
        ParallelFor.par((i) -> {
            int firstDocId = firstDocIds[i];
            String filename = firstdocidFilenameMap.get(firstDocId);
            WikiParser wikiParser = new WikiParser(Util.textPath + filename);
            CompactHashMap<Integer, IntIntPair> local = wikiParser.getDocidPositionMap();
            synchronized (docidPositionMap) {
                docidPositionMap.putAll(local);
            }
        }, 0, firstDocIds.length);

        docidPositionMap.write(Util.docidPosPath);
    }

    static void writeFirstDocidFilenameMap() throws IOException {
        Map<Integer, String> firstDocidFilenameMap = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        for (File dir : Util.textDirs) {
            String[] fileNames = dir.list();
            Arrays.sort(fileNames);
            logger.debug(dir.getAbsolutePath());
            for (String filename : fileNames) {
                WikiParser wikiParser = new WikiParser(dir.getAbsolutePath() + "/" + filename);
                List<Page> pages = wikiParser.getPages();
                int firstDocId = pages.get(0).getId();
                firstDocidFilenameMap.put(firstDocId, filename);
                sb.append(dir.getName()).append("/").append(filename).append(", ").append(firstDocId).append("\n");
            }
        }
        FileUtils.write(new File(Util.firstDocidFilenamePath), sb.toString(), StandardCharsets.UTF_8);
    }

    static Map<Integer, String> readFirstDocidFilenameMap() throws IOException {
        Map<Integer, String> map = new HashMap<>();
        File file = new File(Util.firstDocidFilenamePath);
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String[] ar = content.split("\\R");
        for (String line : ar) {
            String[] ss = line.split(", ");
            String filename = ss[0];
            int id = Integer.parseInt(ss[1]);
            map.put(id, filename);
        }
        return map;
    }
}
