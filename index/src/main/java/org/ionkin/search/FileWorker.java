package org.ionkin.search;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static org.ionkin.search.Util.basePath;

public class FileWorker {

    private static final Logger logger = LoggerFactory.getLogger(FileWorker.class);

    public static Function<Integer, String> articlesText(Map<Integer, Integer> idPositionMap) {
        int[] articleIds = getFileNamesMathesDigits(basePath);
        Arrays.sort(articleIds);

        return articleId -> {
            try {
                int firstArticleIdAtFile = getFileNameWithArticle(articleIds, articleId);
                Pair<Integer, Integer> interval = getInterval(idPositionMap, articleId, firstArticleIdAtFile);
                int startPosition = interval.getKey();
                int endPosition = interval.getValue();

                String fileName = basePath + firstArticleIdAtFile;
                int length = endPosition - startPosition + 1;
                return new String(IO.read(fileName, startPosition * 2, length * 2), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Can't get article text.", e);
                return null;
            }
        };
    }

    /**
     * @return start and end positions
     */
    private static Pair<Integer, Integer> getInterval(Map<Integer, Integer> idPositionMap, int articleId,
                                                      int firstArticleIdAtFile) {
        Integer endPosition = idPositionMap.get(articleId);
        if (endPosition == null) {
            logger.error("article does not exist");
            return null;
        }
        endPosition -= 1;
        logger.debug("endPosition={}", endPosition);

        int startPosition;
        if (firstArticleIdAtFile == articleId) {
            startPosition = 0;
        } else {
            int prevArticle = articleId - 1;
            while (idPositionMap.get(prevArticle) == null) {
                prevArticle -= 1;
            }
            startPosition = idPositionMap.get(prevArticle);
        }
        logger.debug("startPosition={}", startPosition);

        return new Pair<>(startPosition, endPosition);
    }

    private static int getFileNameWithArticle(int[] articleIds, int articleId) {
        int index = Arrays.binarySearch(articleIds, articleId);
        if (index < 0) {
            // articleIds[insertedPoint] > articleId || insertedPoint > lengt
            // insertedPoint = -(index + 1). lowerBound == -(index + 1) - 1 == -index-2
            index = -index - 2;
        }
        logger.debug("fileName for article with id={} is '{}'", articleId, articleIds[index]);
        return articleIds[index];
    }

    public static int[] getFileNamesMathesDigits(String folderName) {
        File folder = new File(folderName);
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .filter(filename -> filename.matches("\\d+"))
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
    }
}
