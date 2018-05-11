package org.ionkin.search;

import org.ionkin.search.map.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class EvaluatorFactory {
    private static Logger logger = LoggerFactory.getLogger(EvaluatorFactory.class);

    static final String basePath = "/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/";
    private CompactHashMap<LightString, byte[]> index;
    private int[] fileIds;
    private String[] titles;
    private CompactHashMap<Integer, Integer> idEndPositionMap;

    public void init() throws IOException {
        index = CompactHashMap.read(basePath + "joined1000Index/compressed", new StringBytesTranslator());
       // titles = org.ionkin.search.DirectIndex.read(DirectIndex.OUT_FILENAME);
       // fileIds = readIds(basePath + "ids/fileIds");
       // Arrays.sort(fileIds);
    }

    public CompactHashMap<Integer, Integer> getIdEndPositionMap() throws IOException {
        if (idEndPositionMap == null) {
            synchronized (this) {
                if (idEndPositionMap == null) {
                    idEndPositionMap = CompactHashMap.read(basePath + "ids/endPositions", new IntIntTranslator());
                }
            }
        }
        return idEndPositionMap;
    }

    public CompactHashMap<LightString, byte[]> getIndex() throws IOException {
        return index;
    }

    public String[] getTitles() throws IOException {
        return titles;
    }

    public int[] getFileIds() throws IOException {
        return fileIds;
    }

    public Evaluator create() throws IOException {
        return new Evaluator(getIndex(), getFileIds());
    }

    private static int[] readIds(String filename) throws IOException {
        byte[] comp = IO.read(filename);
        byte[] sums = Compressor.decompressAndSumInts(comp);
        return IO.readArrayInt(sums, 0, sums.length / 4);
    }

    private static void writeIds(String filename) throws IOException {
        int[] ids = getFileNamesWithArticles(basePath);
        byte[] bytes = IO.toBytes(ids);
        byte[] diff = Compressor.diffInts(bytes);
        byte[] compressed = Compressor.compressInts(diff);
        IO.write(compressed, filename);
    }

    static int[] getFileNamesWithArticles(String folderName) {
        File folder = new File(folderName);
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .filter(filename -> filename.matches("\\d+"))
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
    }
}
