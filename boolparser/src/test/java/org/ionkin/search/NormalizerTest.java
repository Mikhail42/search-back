package org.ionkin.search;

import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizerTest {

    @Test
    public void normalize() {
        assertEquals(Normalizer.normalize("\n  \t \r\n  \r"), "");
        assertEquals(Normalizer.normalize("\n  \ts \r\n  \r"), "s");
        assertEquals(Normalizer.normalize("s \n p"), "s p");
        assertEquals(Normalizer.normalize(" s#я \n p"), "s я p");
        assertEquals(Normalizer.normalize(" !а &&  я  \n"), "!а && я");
    }
}