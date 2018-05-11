package org.ionkin.search;

import com.google.common.primitives.Ints;
import javafx.util.Pair;
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

    private static final String folderPath = "/home/mikhail/workspace/wikiextractor/text/";

    private final Map<Integer, String> firstdocidFilenameMap;
    private final int[] firstDocIds;
    private final CompactHashMap<Integer, Pair<Integer, Integer>> docidPositionMap;

    public TextArticleIterator() throws IOException {
        logger.info("text iterator created. read docid map and positions map");
        firstdocidFilenameMap = readFirstDocidFilenameMap();
        firstDocIds = Ints.toArray(firstdocidFilenameMap.keySet());
        Arrays.sort(firstDocIds);
        //writePositions();

        docidPositionMap = readPositions();
    }

    public Page readPage(int docId) throws IOException {
        int index = getFirstDocIdIndexByDocId(docId);
        int firstDocId = firstDocIds[index];
        logger.debug("first doc for article with id={} is '{}'", docId, firstDocId);
        String filename = folderPath + firstdocidFilenameMap.get(firstDocId);
        Pair<Integer, Integer> startLength = docidPositionMap.get(docId);
        return WikiParser.parsePage(filename, startLength.getKey(), startLength.getValue());
    }

    public int getFirstDocIdIndexByDocId(int docId) {
        int index = Arrays.binarySearch(firstDocIds, docId);
        if (index < 0) {
            // articleIds[insertedPoint] > articleId || insertedPoint > lengt
            // insertedPoint = -(index + 1). lowerBound == -(index + 1) - 1 == -index-2
            index = -index - 2;
        }
        return index;
    }

    public Iterator<Page> articleTextIterator() {
        return articleTextIterator(0);
    }

    public Iterator<Page> articleTextIterator(int firstDocIdIndex0) {
        return new Iterator<Page>() {
            private int firstDocIdIndex = firstDocIdIndex0;
            private Iterator<Page> pageIterator;
            {
                initPageIterator();
            }

            private void initPageIterator() {
                logger.debug("init iterator with index: {}, firstDocId: {}", firstDocIdIndex, firstDocIds[firstDocIdIndex]);
                String filename = folderPath + firstdocidFilenameMap.get(firstDocIds[firstDocIdIndex]);
                logger.debug("init iterator with file: {}", firstdocidFilenameMap.get(firstDocIds[firstDocIdIndex]));
                firstDocIdIndex++;
                try {
                    WikiParser parser = new WikiParser(filename);
                    List<Page> batch = parser.getPages();
                    pageIterator = batch.iterator();
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

    private void writePositions() throws IOException {
        CompactHashMap<Integer, Pair<Integer, Integer>> docidPositionMap =
                new CompactHashMap<>(new IntIntIntTranslator());
        for (int firstDocId : firstDocIds) {
            String filename = firstdocidFilenameMap.get(firstDocId);
            WikiParser wikiParser = new WikiParser(folderPath + filename);
            CompactHashMap<Integer, Pair<Integer, Integer>> local = wikiParser.getDocidPositionMap();
            docidPositionMap.putAll(local);
        }

        String filename = "docPositions.chmiii";
        docidPositionMap.write(filename);
    }

    private CompactHashMap<Integer, Pair<Integer, Integer>> readPositions() throws IOException {
        String filename = this.getClass().getClassLoader().getResource("docPositions.chmiii").getFile();
        return CompactHashMap.read(filename, new IntIntIntTranslator());
    }

    static void writeFirstDocidFilenameMap() throws IOException {
        File folder = new File(folderPath);
        String[] fileNames = folder.list();
        Arrays.sort(fileNames);

        Map<Integer, String> firstDocidFilenameMap = new HashMap<>();
        for (String filename : fileNames) {
            WikiParser wikiParser = new WikiParser(folderPath + filename);
            List<Page> pages = wikiParser.getPages();
            int firstDocId = pages.get(0).getId();
            firstDocidFilenameMap.put(firstDocId, filename);
            System.out.println(filename);
        }
    }

    static Map<Integer, String> readFirstDocidFilenameMap() throws IOException {
        Map<Integer, String> map = new HashMap<>();
        ClassLoader classLoader = WikiParser.class.getClassLoader();
        File file = new File(classLoader.getResource("firstDocidFilenameMap.csv").getFile());
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
