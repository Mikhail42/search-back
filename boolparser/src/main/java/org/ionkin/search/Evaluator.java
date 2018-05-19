package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.SyntaxTree;
import org.scijava.parse.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {
    private static Logger logger = LoggerFactory.getLogger(Evaluator.class);

    private final CompactHashMap<LightString, byte[]> index;
    private final int[] allIds;

    public Evaluator(CompactHashMap<LightString, byte[]> index, int[] allIds) {
        this.index = index;
        this.allIds = allIds;
        logger.debug("Evaluator created");
    }

    public int[] evaluate(String query) {
        SyntaxTree tree = createSyntaxTree(query);
        logger.debug("expression three '{}'", tree);
        return evaluate(tree);
    }

    static SyntaxTree createSyntaxTree(String query) {
        logger.debug("start evaluate '{}'", query);
        String normalized = Normalizer.normalize(query);
        logger.debug("normalized '{}'", normalized);
        Pattern p = Pattern.compile("(/w)/s(/w)");
        Matcher m = p.matcher(normalized);
        if (m.find()) {
            normalized = m.replaceAll("$1 && $2");
        }
        logger.debug("new normalized '{}'", normalized);
        String toExpr = normalized
                .replaceAll("[|]{2,2}", "+")
                .replaceAll("[&]{2,2}", "*")
                .replaceAll("[!]", "-");
        logger.debug("expression '{}'", toExpr);
        return new ExpressionParser().parseTree(toExpr);
    }

    // x & !y || y && x
    private int[] evaluate(SyntaxTree tree) {
        final String token;
        if (tree.token() instanceof Token) {
            token = ((Token) tree.token()).getToken();
        } else {
            token = tree.token().toString();
        }
        logger.debug("token: '{}'", token);
        Iterator<SyntaxTree> trees = tree.iterator();
        switch (token) {
            case "*":
                SyntaxTree and1 = trees.next();
                SyntaxTree and2 = trees.next();
                logger.debug("and1: '{}', and2: '{}'", and1, and2);
                return and(evaluate(and1), evaluate(and2));
            case "+":
                SyntaxTree or1 = trees.next();
                SyntaxTree or2 = trees.next();
                logger.debug("or1: '{}', or2: '{}'", or1, or2);
                return or(evaluate(or1), evaluate(or2));
            case "-":
                SyntaxTree not = trees.next();
                logger.debug("not: '{}'", not);
                return firstAndNotSecond(allIds, evaluate(not));
            case "(":
                SyntaxTree brace = trees.next();
                logger.debug("brace: '{}'", brace);
                return evaluate(brace);
            default:
                return get(new LightString(token));
        }
    }

    int[] get(LightString token) {
        logger.debug("token: {}", token);
        byte[] comp = index.get(token);
        if (comp == null) {
            return new int[0];
        }
        return Compressor.decompressVb(comp);
    }

    static int[] or(int[] ar1, int[] ar2) {
        List<Integer> list = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length) {
            if (ar1[i1] > ar2[i2]) {
                list.add(ar2[i2]);
                i2++;
            } else if (ar1[i1] < ar2[i2]) {
                list.add(ar1[i1]);
                i1++;
            } else {
                list.add(ar1[i1]);
                i1++;
                i2++;
            }
        }
        while (i1 < ar1.length) {
            list.add(ar1[i1]);
            i1++;
        }
        while (i2 < ar2.length) {
            list.add(ar2[i2]);
            i2++;
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    static int[] and(int[] ar1, int[] ar2) {
        List<Integer> list = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length) {
            if (ar1[i1] > ar2[i2]) i2++;
            else if (ar1[i1] < ar2[i2]) i1++;
            else {
                list.add(ar1[i1]);
                i1++;
                i2++;
            }
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    static int[] firstAndNotSecond(int[] ar1, int[] ar2) {
        List<Integer> list = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        while (i1 < ar1.length && i2 < ar2.length) {
            if (ar1[i1] > ar2[i2]) {
                i2++;
            } else if (ar1[i1] < ar2[i2]) {
                list.add(ar1[i1]);
                i1++;
            } else {
                i1++;
                i2++;
            }
        }
        while (i1 < ar1.length) {
            list.add(ar1[i1]);
            i1++;
        }
        return list.stream().mapToInt(i -> i).toArray();
    }
}
