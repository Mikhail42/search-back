package org.ionkin.search;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ColStaticTest {

    @Test
    public void join() {
        HashMap<Integer, ByteArray> glob = new HashMap<>();
        HashMap<Integer, ByteArray> loc = new HashMap<>();
        MeanStd.concatValues(glob, loc);
        assertEquals(glob.size(), 0);

        loc.put(1, new ByteArray(new byte[] {1, 4, 10}));
        MeanStd.concatValues(glob, loc);
        assertEquals(glob.size(), 1);
        assertArrayEquals(glob.get(1).getCopy(), new byte[] {1, 4, 10});

        loc.put(1, new ByteArray(new byte[] {1, 4, 10}));
        MeanStd.concatValues(glob, loc);
        assertEquals(glob.size(), 1);
        assertArrayEquals(glob.get(1).getCopy(), new byte[] {1, 4, 10, 1, 4, 10});

        loc.put(2, new ByteArray(new byte[] {1, 4, 10}));
        MeanStd.concatValues(glob, loc);
        assertEquals(glob.size(), 2);
        assertArrayEquals(glob.get(2).getCopy(), new byte[] {1, 4, 10});
        assertArrayEquals(glob.get(1).getCopy(), new byte[] {1, 4, 10, 1, 4, 10, 1, 4, 10});
    }
}
