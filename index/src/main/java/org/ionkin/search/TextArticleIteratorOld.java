package org.ionkin.search;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.ionkin.search.Util.basePath;

@Deprecated
public class TextArticleIteratorOld {

    private static final Logger logger = LoggerFactory.getLogger(TextArticleIteratorOld.class);

    private final Map<Integer, Integer> idEndPositionMap;
    private final int[] articleIds;
    private final int[] fileIds;

    public TextArticleIteratorOld(Map<Integer, Integer> idEndPositionMap, int[] articleIds) {
        this.articleIds = articleIds;
        this.idEndPositionMap = idEndPositionMap;
        this.fileIds = getFileNamesWithArticles(basePath);
        Arrays.sort(this.fileIds);
    }

    public Iterator<String> articleTextIterator() {
        return new Iterator<String>() {
            private int articleIndex = 0;
            private int fileIndex = 0;
            private byte[] batch;

            private void initBatch() {
                try {
                    batch = IO.read(basePath + fileIds[fileIndex]);
                } catch (IOException ioe) {
                    logger.error("Can't read from {}", basePath + fileIds[fileIndex]);
                }
            }

            private boolean atNextFile() {
                logger.debug("next fileId={}, articleId={}", fileIds[fileIndex+1], articleIds[articleIndex]);
                return (fileIndex+1 < fileIds.length) && fileIds[fileIndex+1] == articleIds[articleIndex];
            }

            @Override
            public boolean hasNext() {
                return articleIndex < articleIds.length;
            }

            @Override
            public String next() {
                if (batch == null) {
                    initBatch();
                }
                if (atNextFile()) {
                    fileIndex++;
                    initBatch();
                }
                Pair<Integer, Integer> interval = getInterval(idEndPositionMap, articleIds[articleIndex], fileIds[fileIndex]);
                int startPosition = interval.getKey();
                int endPosition = interval.getValue();
                int length = endPosition - startPosition + 1;

                articleIndex++;
                return new String(batch, startPosition, length, StandardCharsets.UTF_8);
            }
        };
    }

    /**
     * @return start and end positions
     */
    private static Pair<Integer, Integer> getInterval(Map<Integer, Integer> idEndPositionMap, int articleId,
                                                      int firstArticleIdAtFile) {
        Integer endPosition = idEndPositionMap.get(articleId);
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
            while (idEndPositionMap.get(prevArticle) == null) {
                prevArticle -= 1;
            }
            startPosition = idEndPositionMap.get(prevArticle);
        }
        logger.debug("startPosition={}", startPosition);

        return new Pair<>(startPosition, endPosition);
    }

    private int[] getFileNamesWithArticles(String folderName) {
        File folder = new File(folderName);
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .filter(filename -> filename.matches("\\d+"))
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
    }
}
