package org.ionkin.search;

import org.ionkin.search.map.StringStringMap;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SnippetTest {

    @Test
    public void getSnippetText() {
        int[] range = IntStream.range(1, 100).toArray();
        assertEquals("3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,",
                Snippet.getSnippetText(Arrays.toString(range), 3));
        assertEquals(" 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99]",
                Snippet.getSnippetText(Arrays.toString(range), 90));
    }

    @Test
    public void indexOf() {
        assertEquals(-1, Snippet.indexOf(Pattern.compile("[a-z]").matcher("Ая"), 0));
        assertEquals(3, Snippet.indexOf(Pattern.compile("[a-z]").matcher("Ая rz"), 1));
        assertEquals(7, Snippet.indexOf(Pattern.compile("[a-z]").matcher("Ая rz  sss"), 5));
    }

   /* @Test
    public void selectQueryWords() {
        String snip = Snippet.selectQueryWords("3, 4, 5, 6, 7, 8 9 10, 11, 12", new HashSet<LightString>() {{
            add(new LightString("4"));
            add(new LightString("9"));
            add(new LightString("8"));
        }}, new StringStringMap() {{
            put(new LightString("10"), new LightString("4"));
        }});
        assertEquals("3, <b>4</b>, 5, 6, 7, <b>8 9 10</b>, 11, 12", snip);
    }*/

    @Test
    public void pretty() {
        String pretty = Snippet.pretty("w () w <b>4</b> 5 %");
        assertEquals("w w <b>4</b> 5\u2009%", pretty);
    }

    @Test
    public void optimumSnippetPosition() {
        {
            int[] idfs = new int[]{2};
            int[][] poss = new int[][]{
                    {5, 100, 105}
            };
            assertEquals(100, Snippet.optimumSnippetPosition(poss, idfs));
        }
        {
            int[] idfs = new int[]{500, 2};
            int[][] poss = new int[][]{
                    {5},
                    {100, 150}
            };
            assertEquals(5, Snippet.optimumSnippetPosition(poss, idfs));
        }
        {
            int[] idfs = new int[]{2, 500};
            int[][] poss = new int[][]{
                    {100, 105},
                    {5}
            };
            assertEquals(5, Snippet.optimumSnippetPosition(poss, idfs));
        }
        {
            int[] idfs = new int[]{2, 2};
            int[][] poss = new int[][]{
                    {100, 105},
                    {101, 106}
            };
            assertEquals(100, Snippet.optimumSnippetPosition(poss, idfs));
        }
    }

    @Test
    public void snippet() throws Exception {
        Map<LightString, int[]> wordPos;
        Map<LightString, Integer> idfs;
        Page page = TextArticleIterator.readPage(40002);
    }
}