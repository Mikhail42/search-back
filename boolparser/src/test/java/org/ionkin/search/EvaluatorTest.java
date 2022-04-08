package org.ionkin.search;

import org.ionkin.search.map.SearchMap;
import org.ionkin.search.model.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertArrayEquals;

public class EvaluatorTest {
    private static final Logger logger = LoggerFactory.getLogger(EvaluatorTest.class);

    private EvaluatorPerformance evaluator;

    @Before
    public void before() {
        try {
            evaluator = EvaluatorPerformance.load();
        } catch (Exception e) {
            logger.error("Can't load evaluator", e);
            e.printStackTrace();
        }
    }

    @Test
    public void evaluate() throws IOException {
        List<Pair<Integer, QueryPage>> results = evaluator.evaluate("полку", 5);
        int[] pageIds = results.stream().mapToInt(x -> x.key).toArray();
        assertEquals(5, pageIds.length); // 5 pages (at max) was requested
        //assertArrayEquals(pageIds, new int[]{9, 27, 35, 465, 525});
        SearchMap searchMap = evaluator.getPositions();
        Positions poss = searchMap.get(new LightString("полку"));
        assertNotNull(poss);
        BytesRange range = poss.positions(9); // page #9 should contain this word
        int[] wordPositions = Compressor.decompressVb(range);
        assertTrue(wordPositions.length > 0);
    }

    @Test
    public void wordTest() throws IOException {
        List<Pair<Integer, QueryPage>> results1 = evaluator.evaluate("полку Игореве", 50);
        List<Pair<Integer, QueryPage>> results2 = evaluator.evaluate("\"полку Игореве\"", 50);
        List<Pair<Integer, QueryPage>> results3 = evaluator.evaluate("\"Слово о полку Игореве\"", 50);
        List<Pair<Integer, QueryPage>> results4 = evaluator.evaluate("\"Слово && о полку && Игореве\" / 4", 50);
    }

    @Test
    public void evaluateNotAndOrBrace() throws IOException {
        String query = "!слово && (о || полку)";
        // !слово       == 7, 15, 16, 20, 28, 29, 30, 31, 32, 35, 36, 37, 38, 39, 40, 41, 45, 47, 49, 50, 51, 52
        // (о || полку) == 7, 9, 10, 11, 15, 16, 18, 20, 21, 27, 28, 29, 30, 31, 32, 33, 35, 36, 37, 38, 39, 40
        // ans == 7, 15, 16, 20, 28
        List<Pair<Integer, QueryPage>> results = evaluator.evaluate(query, 5);
        int[] pageIds = results.stream().mapToInt(x -> x.key).toArray();
        assertTrue(pageIds.length > 0 && pageIds.length <= 5); // 5 pages (at max) was requested
    }

    @Test
    public void evaluateQuotes() throws IOException {
        List<Pair<Integer, QueryPage>> results1 = evaluator.evaluate("\"полку Игореве\"", 5);
        int[] pageIds1 = results1.stream().mapToInt(x -> x.key).toArray();
        List<Pair<Integer, QueryPage>> results2 = evaluator.evaluate("полку Игореве", 5);
        int[] pageIds2 = results2.stream().mapToInt(x -> x.key).toArray();
        assertArrayEquals(pageIds1, pageIds2);
        assertEquals(5, pageIds2.length); // 5 pages (at max) was requested
    }
}