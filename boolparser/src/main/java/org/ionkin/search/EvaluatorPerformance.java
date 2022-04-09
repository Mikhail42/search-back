package org.ionkin.search;

import org.ionkin.Ranking;
import org.ionkin.search.map.*;
import org.ionkin.search.model.Pair;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.IntTranslator;
import org.scijava.parse.SyntaxTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EvaluatorPerformance {
    private static final Logger logger = LoggerFactory.getLogger(EvaluatorPerformance.class);
    private static final int BOOL_COUNT = 100_000;

    private final IndexMap indexMap;
    private final IndexMap titleIndex;
    private final SearchMap positions;
    private final SyntaxTreeHelper syntaxTreeHelper;

    public SearchMap getPositions() {
        return positions;
    }

    public static void main(String... args) throws Exception {
        load();
    }

    public static void writeDocIds() throws IOException {
        CompactHashSet<Integer> allPageIds = new CompactHashSet<>(new IntTranslator());
        StringBytesMap titleToPageIds = new StringBytesMap(Util.titleIndexPath);
        titleToPageIds.forEach((k, v) -> {
            int[] pageIds = Compressor.decompressVb(v);
            for (int pageId : pageIds) {
                allPageIds.add(pageId);
            }
        });
        allPageIds.write(Util.docIdsPath);
    }

    public static EvaluatorPerformance load() throws IOException {
        if (!new File(Util.docIdsPath).exists()) {
            writeDocIds();
        }
        byte[] pageIdsAsBytes = IO.read(Util.docIdsPath);
        int[] allPageIds = Compressor.decompressVb(pageIdsAsBytes);

        IndexMap titleIndex = new IndexMap(new StringBytesMap(Util.titleIndexPath));
        IndexMap indexMap = new IndexMap(new StringBytesMap(Util.indexPath));
        // read or write search map
        File searchMapFile = new File(Util.searchMapPath);
        final SearchMap searchMap;
        if (!searchMapFile.exists()) {
            StringPositionsMap positionsMap = new StringPositionsMap(Util.positionsPath);
            searchMap = new SearchMap(positionsMap);
            searchMap.write(searchMapFile.getAbsolutePath());
        } else {
            searchMap = new SearchMap(searchMapFile.getAbsolutePath());
        }

        return new EvaluatorPerformance(searchMap, indexMap, titleIndex, allPageIds);
    }

    private EvaluatorPerformance(SearchMap positions, IndexMap indexMap, IndexMap titleIndex, int[] pageIds) {
        this.indexMap = indexMap;
        this.titleIndex = titleIndex;
        this.positions = positions;
        this.syntaxTreeHelper = new SyntaxTreeHelper(positions, indexMap, pageIds);
        logger.debug("Evaluator created");
    }

    public List<Pair<Integer, QueryPage>> evaluate(String query, int count) throws IOException {
        logger.debug("start evaluate '{}'", query);
        String normalized = Normalizer.normalize(query);
        logger.debug("normalized '{}'", normalized);
        boolean isBool = isBoolQuery(normalized);
        final Collection<LightString> words = getWords(normalized, isBool);

        final Map<LightString, Index> index = indexMap(words);
        final Map<LightString, Index> titleIndex = titleIndexMap(words);
        final Map<LightString, Positions> positions = positionsMap(words);
        final Map<LightString, Integer> idfs = idfs(index);

        final int[] docIds = isBool
                ? evaluate(normalized, idfs, index, titleIndex, positions, count)
                : Logic.simple(idfs, index, titleIndex, positions, BOOL_COUNT, count);

        final Map<Integer, QueryPage> snippets = snippets(docIds, idfs, positions);

        List<Pair<Integer, QueryPage>> res = new LinkedList<>();
        for (int docId : docIds) {
            res.add(new Pair<>(docId, snippets.get(docId)));
        }
        return res;
    }

    private Collection<LightString> getWords(String normalized, boolean isBool) {
        if (isBool) {
            SyntaxTree tree = SyntaxTreeHelper.create(normalized);
            logger.debug("expression three '{}'", tree);
            return SyntaxTreeHelper.queryWords(tree);
        } else {
            return Stream.of(normalized.split(" "))
                    .map(LightString::new)
                    .collect(Collectors.toSet());
        }
    }

    private Map<Integer, QueryPage> snippets(int[] ids, Map<LightString, Integer> idfs,
                                             Map<LightString, Positions> positions) throws IOException {
        return Snippet.snippets(ids, idfs, positions);
    }

    private int[] evaluate(String normalized, Map<LightString, Integer> idfs, Map<LightString, Index> index,
                           Map<LightString, Index> titleIndex, Map<LightString, Positions> positions, int count) {
        SyntaxTree tree = SyntaxTreeHelper.create(normalized);
        int[] docIds0 = syntaxTreeHelper.evaluate(tree, BOOL_COUNT);
        return Logic.ranking(docIds0, idfs, index, titleIndex, positions, count);
    }

    private static boolean isBoolQuery(String query) {
        Pattern pattern = Pattern.compile("[&|!()\\[\\]{}/\"]");
        return pattern.matcher(query).find();
    }

    private Map<LightString, Positions> positionsMap(Collection<LightString> words) {
        Map<LightString, Positions> res = new HashMap<>();
        for (LightString w : words) {
            res.put(w, this.positions.get(w));
        }
        return res;
    }

    private static Map<LightString, Index> indexMap(Collection<LightString> words, IndexMap index) {
        Map<LightString, Index> res = new HashMap<>();
        for (LightString w : words) {
            res.put(w, index.get(w));
        }
        return res;
    }

    private Map<LightString, Index> indexMap(Collection<LightString> words) {
        return indexMap(words, this.indexMap);
    }

    private Map<LightString, Index> titleIndexMap(Collection<LightString> words) {
        return indexMap(words, this.titleIndex);
    }

    private Map<LightString, Integer> idfs(Map<LightString, Index> indexMap) {
        Map<LightString, Integer> res = new HashMap<>();
        indexMap.forEach((k, v) -> res.put(k, Ranking.idf(v.getIndexAsBytes())));
        return res;
    }
}
