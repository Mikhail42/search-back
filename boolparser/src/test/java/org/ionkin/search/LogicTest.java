package org.ionkin.search;

import org.ionkin.search.map.IntBytesMap;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.ionkin.search.Logic.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

public class LogicTest {

    @Test
    public void orTest() {
        int[] or = or(new int[]{1, 3, 5, 20}, new int[]{2, 3, 5, 10}, 50);
        assertArrayEquals(or, new int[]{1, 2, 3, 5, 10, 20});
    }

    @Test
    public void andTest() {
        int[] and = and(new int[]{1, 3, 5}, new int[]{2, 3, 5, 10}, 50);
        assertArrayEquals(and, new int[]{3, 5});
    }

    @Test
    public void firstAndNotSecondTest() {
        int[] ar = firstAndNotSecond(new int[]{1, 3, 5, 15}, new int[]{2, 3, 5, 10}, 50);
        assertArrayEquals(ar, new int[]{1, 15});
    }

    @Test
    public void andQuotes() {
        int[][] wordIndices = new int[][]{
                {10, 15, 50, 100, 1000, 1005, 2005},
                {50, 1000, 2005},
                {1, 12, 20, 25, 40, 50, 67, 102, 1000, 1007, 2005}
        };
        // TODO: it is `white-box` test
        IntBytesMap ibm1 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 100, 1000, 1005});
            addPositions(this, 1000, new int[]{105});
            addPositions(this, 2005, new int[]{57});
        }};
        Positions p1 = new Positions(ibm1);
        IntBytesMap ibm2 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 101, 1000, 1005});
            addPositions(this, 1000, new int[]{106});
            addPositions(this, 2005, new int[]{58});
        }};
        Positions p2 = new Positions(ibm2);
        IntBytesMap ibm3 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 102, 1000, 1005});
            addPositions(this, 1000, new int[]{91});
            addPositions(this, 2005, new int[]{59});
        }};
        Positions p3 = new Positions(ibm3);

        int[] docIds1 = Logic.andQuotes(wordIndices, new Positions[]{p1, p2, p3}, 100, 3);
        assertArrayEquals(docIds1, new int[]{50, 2005});
        int[] docIds2 = Logic.andQuotes(wordIndices, new Positions[]{p1, p2, p3}, 100, 2);
        assertArrayEquals(docIds2, new int[]{});
        int[] docIds3 = Logic.andQuotes(wordIndices, new Positions[]{p1, p2, p3}, 100, 1000);
        assertArrayEquals(docIds3, new int[]{50, 2005});
    }

    @Test
    public void andQuotesNew1() {
        Index[] wordIndices = new Index[]{
                Index.fromOldIndex(new int[]{10, 15, 50, 100, 1000, 1005, 2005}),
                Index.fromOldIndex(new int[]{50, 1000, 2005}),
                Index.fromOldIndex(new int[]{1, 12, 20, 25, 40, 50, 67, 102, 1000, 1007, 2005})
        };
        // TODO: it is `white-box` test
        IntBytesMap ibm1 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 100, 1000, 1005});
            addPositions(this, 1000, new int[]{105});
            addPositions(this, 2005, new int[]{57});
        }};
        Positions p1 = new Positions(ibm1);
        IntBytesMap ibm2 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{8, 17, 101, 1003});
            addPositions(this, 1000, new int[]{106});
            addPositions(this, 2005, new int[]{58});
        }};
        Positions p2 = new Positions(ibm2);
        IntBytesMap ibm3 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 102, 1000, 1005});
            addPositions(this, 1000, new int[]{91});
            addPositions(this, 2005, new int[]{59});
        }};
        Positions p3 = new Positions(ibm3);

        Positions[] arPos = new Positions[]{p1, p2, p3};
        int[] docIds1 = Logic.andQuotes(wordIndices, arPos, 100, 3);
        assertArrayEquals(docIds1, new int[]{50, 2005});
        int[] docIds2 = Logic.andQuotes(wordIndices, arPos, 100, 2);
        assertArrayEquals(docIds2, new int[]{});
        int[] docIds3 = Logic.andQuotes(wordIndices, arPos, 100, 1000);
        assertArrayEquals(docIds3, new int[]{50, 2005});
    }

    @Test
    public void andQuotesNew2() {
        Index[] wordIndices = new Index[]{
                Index.fromOldIndex(new int[]{10, 15, 50, 100, 1000, 1005, 2005}),
                Index.fromOldIndex(new int[]{50, 1000, 2005})
        };
        // TODO: it is `white-box` test
        IntBytesMap ibm1 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{10, 15, 100, 1000, 1005});
            addPositions(this, 1000, new int[]{105});
            addPositions(this, 2005, new int[]{57});
        }};
        Positions p1 = new Positions(ibm1);
        IntBytesMap ibm2 = new IntBytesMap() {{
            addPositions(this, 50, new int[]{8, 17, 101, 1003});
            addPositions(this, 1000, new int[]{10});
            addPositions(this, 2005, new int[]{58});
        }};
        Positions p2 = new Positions(ibm2);

        Positions[] arPos = new Positions[]{p1, p2};
        int[] docIds1 = Logic.andQuotes(wordIndices, arPos, 100, 2);
        assertArrayEquals(docIds1, new int[]{50, 2005});
        int[] docIds2 = Logic.andQuotes(wordIndices, arPos, 100, 1);
        assertArrayEquals(docIds2, new int[]{});
        int[] docIds3 = Logic.andQuotes(wordIndices, arPos, 100, 1000);
        assertArrayEquals(docIds3, new int[]{50, 2005});
    }

    private void addPositions(IntBytesMap ibm, int docId, int[] pos) {
        ibm.put(docId, new BytesRange(Compressor.compressVbWithoutMemory(pos)));
    }

    @Test
    public void isQuotePositive() {
        int[][] wordPositions = new int[][]{
                {10, 15, 100, 1000, 1005},
                {50, 1001, 2005},
                {1, 12, 20, 25, 40, 51, 67, 102, 1002, 1007, 2006}
        };

        assertFalse(Logic.isQuote(wordPositions, 1));
        assertFalse(Logic.isQuote(wordPositions, 2));

        assertTrue(Logic.isQuote(wordPositions, 3));
        assertTrue(Logic.isQuote(wordPositions, 4));
        assertTrue(Logic.isQuote(wordPositions, 10));
    }

    @Test
    public void isQuoteNegative() {
        int[][] wordPositions = new int[][]{
                {10, 15, 100, 1000, 1005},
                {50, 1001, 2005},
                {1, 12, 20, 25, 40, 51, 67, 102, 1002, 1007, 2006},
                {2000}
        };

        assertFalse(Logic.isQuote(wordPositions, 3));
        assertFalse(Logic.isQuote(wordPositions, 2));
        assertFalse(Logic.isQuote(wordPositions, 1));
        assertFalse(Logic.isQuote(wordPositions, 4));
        assertFalse(Logic.isQuote(wordPositions, 10));
        assertFalse(Logic.isQuote(wordPositions, 900));
        assertFalse(Logic.isQuote(wordPositions, 999));

        assertTrue(Logic.isQuote(wordPositions, 1000));
    }
}
