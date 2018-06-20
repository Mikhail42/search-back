package org.ionkin.search;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ionkin.search.SyntaxTreeHelper.replaceSpaceOnAnd;
import static org.junit.Assert.*;

public class SyntaxTreeHelperTest {

    @Test
    public void replaceSpaceOnAndTest() {
        assertEquals(replaceSpaceOnAnd("a b"), "a && b");
        assertEquals(replaceSpaceOnAnd("a && b"), "a && b");
        assertEquals(replaceSpaceOnAnd("a || b"), "a || b");
        assertEquals(replaceSpaceOnAnd("ab bc cd"), "ab && bc && cd");
        assertEquals(replaceSpaceOnAnd("a b c"), "a && b && c");

        assertEquals(replaceSpaceOnAnd("что где когда"), "что && где && когда");
        assertEquals(replaceSpaceOnAnd("чт гд ко"), "чт && гд && ко");
        assertEquals(replaceSpaceOnAnd("чт г ко"), "чт && г && ко");
        assertEquals(replaceSpaceOnAnd("и государственные"),
                "и && государственные");
        assertEquals(replaceSpaceOnAnd("партийные и государственные"),
                "партийные && и && государственные");
        assertEquals(replaceSpaceOnAnd("официальные партийные и государственные"),
                "официальные && партийные && и && государственные");
    }
}