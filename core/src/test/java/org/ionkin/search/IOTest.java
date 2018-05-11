package org.ionkin.search;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class IOTest {

    @Test
    public void testWriteRead() throws IOException {
        File tempFile = File.createTempFile("MyAppName-", ".tmp");
        tempFile.deleteOnExit();
        String filename = tempFile.getAbsolutePath();
        byte[] ar = new byte[]{1, 2, 10, 10, 34, 52, 0, -32, 23};
        IO.write(ar, filename);
        byte[] ar2 = IO.read(filename);
        assertTrue(Arrays.equals(ar, ar2));
        byte[] ar3 = IO.read(filename, 2, 2);
        assertTrue(Arrays.equals(ar3, new byte[]{10, 10}));
    }

    @Test
    public void testPutReadInt() {
        byte[] ar = new byte[10];
        IO.putInt(ar, -23554, 3);
        assertEquals(IO.readInt(ar, 3), -23554);
    }

    @Test
    public void testPutReadString() {
        byte[] ar = new byte[10];
        IO.putString(ar, new LightString("мама"), 3);
        assertEquals(IO.readString(ar, 3), new LightString("мама"));
    }

    @Test
    public void testReadArrayInt() {
        byte[] bytes = new byte[]{0, 0, 1, 13, 0, 0, 0, -2};
        int[] ints = IO.readArrayInt(bytes, 0, 2);
        assertTrue(Arrays.equals(ints, new int[]{256 + 13, 256 - 2}));
    }

    @Test
    public void testReadArrayIntWithLength() {
        byte[] bytes = new byte[]{0, 0, 0, 2, 0, 0, 1, 13, 0, 0, 0, -2};
        int[] ints = IO.readArrayIntWithLength(bytes, 0);
        assertTrue(Arrays.equals(ints, new int[]{256 + 13, 256 - 2}));
    }

    @Test
    public void testToIntsToBytes() {
        byte[] ar1 = new byte[]{0, 0, 0, 2, 0, 0, 1, 13, 0, 0, 0, -2};
        byte[] ar2 = IO.toBytes(IO.toInts(ar1));
        assertTrue(Arrays.equals(ar1, ar2));
    }

//    @Test
    public void speed() throws IOException {
        long s = System.currentTimeMillis();
        IO.read("/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/joined1000Index/compressed");
        System.err.println(System.currentTimeMillis() - s);
    }

    @Test
    public void putStringWithLength() {
        String s = "abcd";
        byte[] asd = s.getBytes(StandardCharsets.UTF_16);
        byte[] ar = new byte[50];
        IO.putStringWithLength(ar, s, 0);
        int i = 5;
    }
}