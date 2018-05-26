package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringBytesMap;
import org.ionkin.search.map.StringBytesTranslator;
import org.junit.Test;
import org.scijava.parse.SyntaxTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class EvaluatorTest {

    @Test
    public void createSyntaxTree() {
        String query = "!word && (a || b) && c || p";
        SyntaxTree syntaxTree = EvaluatorPerformance.createSyntaxTree(query);
        assertEquals(syntaxTree.token().toString(), "+");
        assertEquals(syntaxTree.child(1).toString(), " 'p'\n");

        SyntaxTree child0 = syntaxTree.child(0);
        assertEquals(child0.token().toString(), "*");

        SyntaxTree grandchild01 = child0.child(1);
        assertEquals(grandchild01.toString(), " 'c'\n");

        SyntaxTree grandchild00 = child0.child(0);
        assertEquals(grandchild00.token().toString(), "*");
        assertEquals(grandchild00.child(0).token().toString(), "-");
        assertEquals(grandchild00.child(0).child(0).toString(), " 'word'\n");
        assertEquals(grandchild00.child(1).token().toString(), "(1)");

        SyntaxTree grandchild0010 = grandchild00.child(1).child(0);
        assertEquals(grandchild0010.token().toString(), "+");
        assertEquals(grandchild0010.child(0).toString(), " 'a'\n");
        assertEquals(grandchild0010.child(1).toString(), " 'b'\n");
    }

    private final StringBytesMap index = new StringBytesMap() {{
        put(new LightString("word"), compress(1, 5));
        put(new LightString("a"), compress(1, 3, 4));
        put(new LightString("b"), compress(2, 3));
        put(new LightString("c"), compress(5));
        put(new LightString("p"), compress(1, 10));
    }};
    private final int[] allIds = new int[]{1, 2, 3, 4, 5, 10};
    private final EvaluatorPerformance evaluator = new EvaluatorPerformance(index, allIds);

    // TODO: try to use another library to simplify expression
    @Test
    public void evaluateComplexQuery() {
        evaluator.evaluate("!\"word a\" / 4 || [v && (c b)]", 50);
    }

    @Test
    public void evaluate() {
        String query = "!word && (a || b) && c || p";
        // !word == 2, 3, 4, 10
        // (a || b) == 1, 2, 3, 4
        // !word && (a || b) == 2, 3, 4
        // !word && (a || b) && c == {}
        // !word && (a || b) && c || p == p == 1, 10
        int[] indices = evaluator.evaluate(query, 50);
        assertTrue(Arrays.equals(indices, new int[]{1, 10}));
    }

    private byte[] compress(int... ar) {
        return Compressor.compressVbWithoutMemory(ar);
    }

    @Test
    public void get() {
        int[] ar = evaluator.get(new LightString("a"), 50);
        assertTrue(Arrays.equals(ar, new int[]{1, 3, 4}));
    }

    @Test
    public void or() {
        int[] or = EvaluatorPerformance.or(new int[]{1, 3, 5}, new int[]{2, 3, 5, 10}, 50);
        assertTrue(Arrays.equals(or, new int[]{1, 2, 3, 5, 10}));
    }

    @Test
    public void and() {
        int[] and = EvaluatorPerformance.and(new int[]{1, 3, 5}, new int[]{2, 3, 5, 10}, 50);
        assertTrue(Arrays.equals(and, new int[]{3, 5}));
    }

    @Test
    public void firstAndNotSecond() {
        int[] ar = EvaluatorPerformance.firstAndNotSecond(new int[]{1, 3, 5}, new int[]{2, 3, 5, 10}, 50);
        assertTrue(Arrays.equals(ar, new int[]{1}));
    }
}