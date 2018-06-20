package org.ionkin.search;

import javafx.util.Pair;
import org.ionkin.Ranking;
import org.ionkin.search.map.*;
import org.scijava.parse.SyntaxTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EvaluatorPerformance {
    private static Logger logger = LoggerFactory.getLogger(EvaluatorPerformance.class);
    private static final int BOOL_COUNT = 100_000;

    private final IndexMap indexMap;
    private final IndexMap titleIndex;
    private final SearchMap positions;
    private final StringStringMap lemms;
    private final SyntaxTreeHelper syntaxTreeHelper;

    public SearchMap getPositions() {
        return positions;
    }

    public StringStringMap getLemms() {
        return lemms;
    }

    public static void main(String... args) throws Exception {
        // TODO
        loadTest();
    }

    public static EvaluatorPerformance loadTestTest() throws IOException {
        byte[] docsAsBytes = IO.read(Util.basePath + "docids.chsi");
        int[] allIds = Compressor.decompressVb(docsAsBytes);

        IndexMap indexMap = new IndexMap(new StringBytesMap(Util.basePath + "test.index"));
        IndexMap titleIndexMap = new IndexMap(new StringBytesMap(Util.basePath + "testTitle.index"));
        SearchMap searchMap = new SearchMap(new StringPositionsMap(Util.basePath + "test.posit"));
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);

        return new EvaluatorPerformance(searchMap, indexMap, titleIndexMap, allIds, lemms);
    }

    public static EvaluatorPerformance loadTest() throws IOException {
        byte[] docsAsBytes = IO.read(Util.basePath + "docids.chsi");
        int[] allIds = Compressor.decompressVb(docsAsBytes);

        IndexMap indexMap = new IndexMap(Util.basePath + "indexlemm.im");
        IndexMap titleIndex = new IndexMap(new StringBytesMap(Util.basePath + "titleindex.sbm"));
        SearchMap searchMap = new SearchMap(Util.basePath +  "positionslemm.sm");
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);

        return new EvaluatorPerformance(searchMap, indexMap, titleIndex, allIds, lemms);
    }

    private EvaluatorPerformance(SearchMap positions, IndexMap indexMap, IndexMap titleIndex, int[] allIds, StringStringMap lemms) {
        this.indexMap = indexMap;
        this.titleIndex = titleIndex;
        this.positions = positions;
        this.lemms = lemms;
        syntaxTreeHelper = new SyntaxTreeHelper(positions, indexMap, lemms, allIds);
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
            return SyntaxTreeHelper.queryWords(tree).stream()
                    .map(w -> lemms.getOrDefault(w, w)).collect(Collectors.toList());
        } else {
            return Stream.of(normalized.split(" "))
                    .map(LightString::new)
                    .map(x -> this.lemms.getOrDefault(x, x))
                    .collect(Collectors.toSet());
        }
    }

    private Map<Integer, QueryPage> snippets(int[] ids, Map<LightString, Integer> idfs,
                                             Map<LightString, Positions> positions) throws IOException {
        return Snippet.snippets(ids, idfs, positions, this.lemms);
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
