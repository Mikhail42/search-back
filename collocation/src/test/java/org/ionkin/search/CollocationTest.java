package org.ionkin.search;

import org.junit.Before;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CollocationTest {

    private Collocation col;

    @Before
    public void before() throws IOException {
        col = new Collocation();
    }

    /*@Test
    public void initTest() {
        for (int i = 0; i < 100; i++) {
            assertEquals(col.siRelat.get(col.isRelat.get(i)).intValue(), i);
        }
    }*/
}