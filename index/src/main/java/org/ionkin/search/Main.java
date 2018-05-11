package org.ionkin.search;

import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);
    static final String basePath = "/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/";

    public static void main(String... args) throws Exception {
        int[] articleIds = getFileNamesWithArticles(Util.basePath + "articleWords2");
        Arrays.sort(articleIds);
        createInvertIndex(100_000, articleIds);
    }

    public static int[] getFileNamesWithArticles(String folderName) {
        File folder = new File(folderName);
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .filter(filename -> filename.matches("/d+"))
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public static void createInvertIndex(int hundredsThousands, int[] articleIds) throws Exception {
        logger.debug("createInvertIndex {}", hundredsThousands);

        final int HUNDRED_SHOUSANDs = 1000 * 10;
        int minId = hundredsThousands * HUNDRED_SHOUSANDs;
        int maxId = (hundredsThousands + 1) * HUNDRED_SHOUSANDs;

        int index = Arrays.binarySearch(articleIds, minId);
        if (index == -1) index = 0;
        if (index < 0) {
            // articleIds[insertedPoint] > articleId || insertedPoint > lengt
            // insertedPoint = -(index + 1). lowerBound == -(index + 1) - 1 == -index-2
            index = -index - 2;
        }
        HashMap<LightString, ArrayList<Integer>> invertIndex = new HashMap<>();
        logger.debug("hash map created");
        while (index < articleIds.length && articleIds[index] < maxId) {
            int articleId = articleIds[index];
            if (articleId >= minId) {
                CompactHashSet<LightString> set =
                        CompactHashSet.read(Util.basePath + "articleWords2/" + articleId,
                                new StringTranslator());
                set.forEach(s -> {
                    ArrayList<Integer> ids = invertIndex.getOrDefault(s, new ArrayList<>());
                    ids.add(articleId);
                    invertIndex.put(s, ids);
                });
            }
            index++;
        }
        invertIndex.remove(new LightString(""));
        HashMap<LightString, int[]> invertIndexLight = new HashMap<>();
        invertIndex.forEach((k, v) -> invertIndexLight.put(k, v.stream().mapToInt(i->i).toArray()));
        invertIndexLight.remove(new LightString(""));
        logger.debug("write to {}", Util.basePath + "invertedIndex/" + hundredsThousands);
        Serializer.serialize(invertIndexLight, Util.basePath + "invertedIndex2/" + hundredsThousands);
    }
}
