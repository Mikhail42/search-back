package org.ionkin.search;

import org.ionkin.search.map.IndexMap;
import org.ionkin.search.map.SearchMap;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.SyntaxTree;
import org.scijava.parse.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ionkin.search.Logic.and;
import static org.ionkin.search.Logic.firstAndNotSecond;
import static org.ionkin.search.Logic.or;
import static org.scijava.parse.Operators.*;

public class SyntaxTreeHelper {
    private static final Logger logger = LoggerFactory.getLogger(SyntaxTreeHelper.class);

    private static final String closeBrace = "]\\)}\"";

    static SyntaxTree create(String normalized) {
        normalized = replaceSpaceOnAnd(normalized);
        logger.debug("normalized '{}'", normalized);
        return new ExpressionParser().parseTree(normalized);
    }

    static String replaceSpaceOnAnd(String s) {
        Pattern p = Pattern.compile("([" + closeBrace + Util.searchableSymbols + "]) ([^&|])");
        Matcher m = p.matcher(s);
        while (m.find()) {
            s = m.replaceAll("$1 && $2");
            m = p.matcher(s);
        }
        return s;
    }

    private final SearchMap positions;
    private final IndexMap indexMap;
    private final int[] allIds;

    public SyntaxTreeHelper(SearchMap positions, IndexMap indexMap, int[] allIds) {
        this.positions = positions;
        this.indexMap = indexMap;
        this.allIds = allIds;
    }

    int[] evaluate(SyntaxTree tree, int count) {
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
            return get(ls, count);
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
     * @param distance max DISTANCE between first and last word at sequence
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

    private static List<Object> allTokens(SyntaxTree tree) {
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
                .collect(Collectors.toList());
        for (LightString w : words) {
            if (indexMap.get(w) == null) {
                throw new IllegalArgumentException("Exists non searchable word (word does not exists at wiki): " + w.toString());
            }
        }
        return words;
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
}
