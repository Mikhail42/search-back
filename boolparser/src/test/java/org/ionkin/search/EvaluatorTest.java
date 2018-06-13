package org.ionkin.search;

import org.ionkin.search.map.SearchMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class EvaluatorTest {
    private EvaluatorPerformance evaluator;

   /* @Before
    public void before() {
        try {
            evaluator = EvaluatorPerformance.loadTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get() {
        LightString key = new LightString("полку");
        int[] ar = evaluator.get(key, 5);
        assertTrue(Arrays.equals(ar, new int[]{9, 27, 35, 465, 525}));
        SearchMap searchMap = evaluator.getPositions();
        Positions poss = searchMap.get(key);
        assertNotNull(poss);
        BytesRange range = poss.positions(9);
        int[] arPos = Compressor.decompressS9(range);

        int a = 5;
    }

    @Test
    public void wordtest() {
        int[] ids = evaluator.evaluateDocIds("полку Игореве", 50);
        int[] polk = evaluator.evaluateDocIds("«полку Игореве»", 50);
        evaluator.evaluateDocIds("«Слово о полку Игореве»", 50);
        evaluator.evaluateDocIds("«Слово && о полку && Игореве» / 4", 50);
    }

    @Test
    public void evaluateNotAndOrBrace() {
        String query = "!слово && (о || полку)";
        // !слово       == 7, 15, 16, 20, 28, 29, 30, 31, 32, 35, 36, 37, 38, 39, 40, 41, 45, 47, 49, 50, 51, 52
        // (о || полку) == 7, 9, 10, 11, 15, 16, 18, 20, 21, 27, 28, 29, 30, 31, 32, 33, 35, 36, 37, 38, 39, 40
        // ans == 7, 15, 16, 20, 28
        int[] indices = evaluator.evaluateDocIds(query, 5);
        assertTrue(Arrays.equals(indices, new int[]{7, 15, 16, 20, 28}));
    }

    @Test
    public void evaluateQuotes() {
        int[] indices1 = evaluator.evaluateDocIds("«полку Игореве»", 5);
        int[] indices2 = evaluator.evaluateDocIds("полку Игореве", 5);
        assertTrue(Arrays.equals(indices1, indices2));
        assertTrue(Arrays.equals(indices1, new int[]{9, 27, 465, 525, 860}));
    }*/
}