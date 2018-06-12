package org.ionkin.search;

import org.ionkin.search.map.*;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.SyntaxTree;
import org.scijava.parse.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ionkin.search.Logic.*;
import static org.scijava.parse.Operators.*;

public class EvaluatorPerformance {
    private static Logger logger = LoggerFactory.getLogger(EvaluatorPerformance.class);

    private final int[] allIds;
    private final IndexMap indexMap;
    private final SearchMap positions;
    //private final StringStringMap lemms;

    public static void main(String... args) throws Exception {
        //writeTestMap();
        EvaluatorPerformance evaluator = load(Util.basePath + "test.index", Util.basePath + "test.posit");
        //new IndexMap(new StringBytesMap(Util.basePath + "indexlemm.sbm")).write(Util.basePath + "indexlemm.im");
        //new SearchMap(new StringPositionsMap(Util.basePath + "positionslemm.spm")).write(Util.basePath + "positionslemm.sm");
        long t1 = System.currentTimeMillis();
        logger.info(Arrays.toString(evaluator.evaluate("«об авторских правах»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Слово о полку Игореве»", 50)));
      //  logger.info(Arrays.toString(evaluator.evaluate(" «что  где  когда»  &&  !«хрустальная  сова»", 50)));
      //  logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»", 50)));
      //  logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»  /  5", 50)));
      //  logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»    &&    друзь", 50)));
      //  logger.info(Arrays.toString(evaluator.evaluate(" «что  где  когда»  ||    квн", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«война и мир»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«1 российский фильм»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Петр Великий»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Двенадцать стульев»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Булев поиск»", 50)));
        logger.info("middle time: {}", (System.currentTimeMillis() - t1) / 12);
    }

    private static void writeTestMap() throws Exception {
        //StringStringMap lemms = new StringStringMap(Util.wordLemmPath);
        Stream<LightString> words = Stream.of(("об авторских правах Слово о полку Игореве что где когда хрустальная" +
                " сова война и мир 1 российский фильм Петр Великий Двенадцать стульев друзь квн Булев поиск").split(" "))
                .map(Normalizer::normalize).map(LightString::new);
                //.map(w -> lemms.getOrDefault(w, w)); // TODO
        StringBytesMap indexMap = new StringBytesMap(Util.basePath + "index.chmsb");
        StringBytesMap testIm = new StringBytesMap();
        StringPositionsMap searchMap = new StringPositionsMap(Util.basePath + "positions.chmsp");
        StringPositionsMap testSm = new StringPositionsMap();
        words.forEach(w -> {
            testSm.put(w, searchMap.get(w));
            testIm.put(w, indexMap.get(w));
        });
        testIm.write(Util.basePath + "test.index");
        testSm.write(Util.basePath + "test.posit");
        /*int[] ar1 = Compressor.decompressVb(testSm.get(new LightString("правах")).get(2705));
        logger.info(Arrays.toString(ar1));
        int[] ar2 = Compressor.decompressVb(searchMap.get(new LightString("правах")).get(2705));
        logger.info(Arrays.toString(ar2));*/
    }

    @Deprecated
    public static EvaluatorPerformance load() throws IOException {
        return load(Util.basePath + "index.im", Util.basePath + "positions.sm");
    }

    @Deprecated
    private static void writeTestMaps() throws Exception {
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);
        List<LightString> words =
                Stream.of("война", "и", "мир", "российский", "фильм", "1", "первого", "театральное", "искусство")
                        .map(LightString::new).map(w -> lemms.getOrDefault(w, w))
                        .collect(Collectors.toList());

        StringPositionsMap spMap = new StringPositionsMap(Util.positionsPath);
        StringPositionsMap testSearch = new StringPositionsMap();
        words.forEach(w -> testSearch.put(w, spMap.get(w)));
        testSearch.write(Util.testPositionsPath);

        StringBytesMap indexMap = new StringBytesMap(Util.testIndexPath);
        StringBytesMap testIndexMap = new StringBytesMap();
        words.forEach(w -> testIndexMap.put(w, indexMap.get(w)));
        testIndexMap.write(Util.testIndexPath);
    }

    public static EvaluatorPerformance loadTest() throws IOException {
        return load(Util.indexFolder + "AA f", Util.positionIndexFolder + "AA f");
    }

    private static void loadWithTest(String indexPath, String positionsPath) throws Exception {
        StringBytesMap sbMap = new StringBytesMap(indexPath);
        IndexMap indexMap = new IndexMap(sbMap);

        StringPositionsMap spMap = new StringPositionsMap(positionsPath);
        SearchMap searchMap = new SearchMap(spMap);

        LightString war = new LightString("война");
        int[] warIds = Compressor.decompressVb(sbMap.get(war));
        for (int id : warIds) {
            logger.info("id: {}", id);
            IntBytesMap ibm = spMap.get(war);
            BytesRange br = ibm.get(id);
            int[] ar1 = Compressor.decompressVb(br);
            /*if (id == 256) {
                int a = 5;
            }*/
            int[] ar2 = Compressor.decompressVb(searchMap.get(war).positions(id));
            boolean f = Arrays.equals(ar1, ar2);
            logger.info("id: {}", f);
        }
        logger.info(Arrays.toString(Compressor.decompressVb(spMap.get(war).get(9))));
        logger.info(Arrays.toString(Compressor.decompressVb(spMap.get(war).get(6040))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(7))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(7))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(9))));
    }

    private static void speedTest(StringPositionsMap spMap) {
        AtomicLong totalPos = new AtomicLong();
        AtomicLong totalDocIds = new AtomicLong();
        spMap.forEach((k, v) -> {
            v.forEach((did, pos) -> {
                int n = 0;
                for (int i = 0; i < pos.length(); i++) n += pos.get(i) < 0 ? 1 : 0;
                totalPos.addAndGet(n);
                if (n == 0) throw  new RuntimeException("pos length is zero: k=" + k + " " + did);
                totalDocIds.addAndGet(1);
            });
        });
        logger.info("TOTAL POS: {}", totalPos.get());
        logger.info("KEY-DOCID PAIRS: {}", totalDocIds.get());
        logger.info("SIZE: {}", spMap.size());
    }

    private static EvaluatorPerformance load(String indexPath, String positionsPath) throws IOException {
        byte[] docsAsBytes = IO.read(Util.basePath + "docids.chsi");
        int[] allIds = Compressor.decompressVb(docsAsBytes);

        IndexMap indexMap = new IndexMap(new StringBytesMap(indexPath));

        StringPositionsMap spm = new StringPositionsMap(positionsPath);
        SearchMap searchMap = new SearchMap(spm);

  /*      IntBytesMap ibm = spm.get(new LightString("правах"));
        Positions poss = new Positions(ibm);
        logger.info(Arrays.toString(Compressor.decompressVb(poss.positions(2705))));
        logger.info(Arrays.toString(Compressor.decompressVb(ibm.get(2705))));
*/
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);

        return new EvaluatorPerformance(searchMap, indexMap, allIds, lemms);
    }

    public EvaluatorPerformance(SearchMap positions, IndexMap indexMap, int[] allIds, StringStringMap lemms) {
        this.indexMap = indexMap;
        this.allIds = allIds;
        this.positions = positions;
        //this.lemms = lemms; // TODO
        logger.debug("Evaluator created");
    }

    public SearchMap getPositions() {
        return positions;
    }

    public int[] evaluate(String query, int count) {
        SyntaxTree tree = createSyntaxTree(query);
        logger.debug("expression three '{}'", tree);
        int[] docIds0 = evaluate(tree, count);//TODO
        //List<LightString> wordsAsList =  queryWords(tree);
        //LightString[] words = wordsAsList.toArray(new LightString[0]);
        return docIds0;//Logic.rankingTfIdf(docIds0, words, this.positions, this.indexMap, count);
    }

    static boolean isBoolQuery(String query) {
        Pattern pattern = Pattern.compile("[&|!()\\[\\]{}/«»\"]");
        return pattern.matcher(query).find();
    }

    static SyntaxTree createSyntaxTree(String query) {
        logger.debug("start evaluate '{}'", query);
        String normalized = Normalizer.normalize(query);
        logger.debug("normalized '{}'", normalized);
        if (isBoolQuery(query)) {
            //normalized = normalized.replaceAll(" ", " && ");
            Matcher spaceMatcher = Pattern.compile(" ([^&|/])").matcher(normalized);
            if (spaceMatcher.find()) {
                normalized = spaceMatcher.replaceAll(" && $1");
            }
            logger.debug("normalized '{}'", normalized);
            Matcher andLeftMatcher = Pattern.compile("&& ([&|/])").matcher(normalized);
            if (andLeftMatcher.find()) {
                normalized = andLeftMatcher.replaceAll("$1");
            }
            logger.debug("normalized '{}'", normalized);
            Matcher andRightMatcher = Pattern.compile("([&|/]) &&").matcher(normalized);
            if (andRightMatcher.find()) {
                normalized = andRightMatcher.replaceAll("$1");
            }
        } else {
            normalized = normalized.replaceAll(" ", " || ");
        }
        logger.debug("normalized '{}'", normalized);
        return new ExpressionParser().parseTree(normalized);
    }

    static List<LightString> queryWords(SyntaxTree tree) {
        final Object tokenObj = tree.token();
        final String token = tokenObj.toString();
        logger.debug("token: '{}'", token);
        if (Tokens.isGroup(tokenObj)) {
            return queryWords(tree.child(0));
        } else if (Tokens.isOperator(tokenObj)) {
            switch (token) {
                case "&&":
                case "||":
                    List<LightString> res = queryWords(tree.child(0));
                    res.addAll(queryWords(tree.child(1)));
                    return res;
                case "!":
                    return new LinkedList<>();
                case "/":
                    return queryWords(tree.child(0));
                default:
                    throw new IllegalArgumentException("Unexpected operator: " + token);
            }
        } else {
            return new LinkedList<LightString>() {{
                add(new LightString(token));
            }};
        }
    }

    private int[] evaluate(SyntaxTree tree, int count) {
        final Object tokenObj = tree.token();
        final String token = tokenObj.toString();
        logger.debug("token: '{}'", token);
        if (Tokens.isGroup(tokenObj)) {
            if (Tokens.isMatchingGroup(tokenObj, PARENS) || Tokens.isMatchingGroup(tokenObj, BRACKETS)
                    || Tokens.isMatchingGroup(tokenObj, BRACES)) {
                return onBrace(tree, count);
            } else if (Tokens.isMatchingGroup(tokenObj, QUOTES)) {
                return onQuotes(tree, count);
            } else {
                throw new IllegalArgumentException("Unexpected group: " + token);
            }
        } else if (Tokens.isOperator(tokenObj) && !Tokens.isGroup(tokenObj)) {
            switch (token) {
                case "&&":
                    SyntaxTree and1 = tree.child(0);
                    SyntaxTree and2 = tree.child(1);
                    logger.debug("and1: '{}', and2: '{}'", and1, and2);
                    return and(evaluate(and1, Integer.MAX_VALUE), evaluate(and2, Integer.MAX_VALUE), count);
                case "||":
                    SyntaxTree or1 = tree.child(0);
                    SyntaxTree or2 = tree.child(1);
                    logger.debug("or1: '{}', or2: '{}'", or1, or2);
                    return or(evaluate(or1, count), evaluate(or2, count), count);
                case "!":
                    SyntaxTree not = tree.child(0);
                    logger.debug("not: '{}'", not);
                    return firstAndNotSecond(allIds, evaluate(not, Integer.MAX_VALUE), count);
                case "/":
                    return onQuotesWithDistance(tree, count);
                default:
                    throw new IllegalArgumentException("Unexpected operator: " + token);
            }
        } else {
            LightString ls = new LightString(token);
            return get(ls/*lemms.getOrDefault(ls, ls) TODO*/, count);
        }
    }

    int[] onBrace(SyntaxTree tree, int count) {
        logger.debug("brace: '{}'", tree);
        return evaluate(tree.iterator().next(), count);
    }

    int[] onQuotes(SyntaxTree tree, int count) {
        List<Object> tokens = allTokens(tree.iterator().next());
        List<LightString> words = findWikiWords(tokens); // TODO: handle exception
        for (LightString w : words) {
            logger.info("word at qoutes: {}", w);
        }
        return andQuotes(words, count, words.size());
    }

    /**
     * @param words    word at quotes. Order is very impotent
     * @param count    max count of indices to return
     * @param distance max distance between first and last word at sequence
     * @see Logic#andQuotes
     */
    int[] andQuotes(List<LightString> words, int count, int distance) {
        Index[] wordDocIds = new Index[words.size()];
        Positions[] poss = new Positions[words.size()];
        for (int i = 0; i < words.size(); i++) {
            LightString word = words.get(i);
            wordDocIds[i] = indexMap.get(word);
            poss[i] = positions.get(word);
        }
        return Logic.andQuotes(wordDocIds, poss, count, distance);
    }

    int[] onQuotesWithDistance(SyntaxTree tree, int count) {
        SyntaxTree quotesTree = tree.child(0);
        SyntaxTree numTree = tree.child(1);
        int distance = Integer.parseInt(numTree.token().toString());

        List<Object> tokens = allTokens(quotesTree);
        List<LightString> words = findWikiWords(tokens); // TODO: handle exception
        return andQuotes(words, count, distance);
    }

    int[] get(LightString token, int count) {
        logger.trace("token: {}", token);
        Index index = indexMap.get(token);
        return index.getIndex(count);
    }

    private List<Object> allTokens(SyntaxTree tree) {
        List<Object> acc = new LinkedList<>();
        Object token = tree.token();
        if (token != null) {
            acc.add(token);
            tree.forEach(t -> acc.addAll(allTokens(t)));
        }
        return acc;
    }

    private List<LightString> findWikiWords(List<Object> tokens) {
        List<LightString> words = tokens.stream()
                .filter(t -> !Tokens.isOperator(t))
                .map(Object::toString)
                .map(Normalizer::normalize)
                .map(LightString::new)
                //.map(w -> lemms.getOrDefault(w, w)) TODO
                .collect(Collectors.toList());
        for (LightString w : words) {
            if (indexMap.get(w) == null) {
                throw new IllegalArgumentException("Exists non searchable word (word does not exists at wiki): " + w.toString());
            }
        }
        return words;
    }

}
