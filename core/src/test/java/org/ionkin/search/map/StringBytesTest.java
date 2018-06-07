package org.ionkin.search.map;

import org.ionkin.search.BytesRange;
import org.ionkin.search.Compressor;
import org.ionkin.search.LightString;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * Compact hash map test
 *
 * Copyright (c) 2015 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/compact-hash-map-java
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

public class StringBytesTest {

    /**
     * @author M. Ionkin
     */
    @Test
    public void testSer() throws IOException {
        StringBytesMap map = new StringBytesMap();
        BytesRange ar1 = new BytesRange(new byte[]{1, 5, 9, 3, 2});
        map.put(new LightString("мама"), ar1);
        byte[] ser = map.fastSerialization();

        StringBytesMap map2 = new StringBytesMap(ser);
        BytesRange ar2 = map2.get(new LightString("мама"));
        assertEquals(ar1, ar2);
    }

    /**
     * @author M. Ionkin
     */
    @Test
    public void testWriteRead() throws IOException {
        File file = File.createTempFile("MyApp", "tmp");
        file.deleteOnExit();
        String filename = file.getAbsolutePath();

        StringBytesMap map = new StringBytesMap();
        BytesRange ar1 = new BytesRange(new byte[]{1, 5, 9, 3, 2});
        map.put(new LightString("мама"), ar1);
        map.write(filename);

        StringBytesMap map2 = new StringBytesMap();
        map2.read(filename);
        BytesRange ar2 = map2.get(new LightString("мама"));
        assertEquals(ar1, ar2);
    }

    /**
     * @author M. Ionkin
     */
    @Test
    public void joinStringBytes() throws Exception {
        LightString word = new LightString("word");
        LightString mir = new LightString("mir");
        StringBytesMap ib1 = new StringBytesMap();
        ib1.put(word, new BytesRange(Compressor.compressVbWithoutMemory(new int[]{1, 3, 5, 6})));
        ib1.put(mir, new BytesRange(Compressor.compressVbWithoutMemory(new int[]{1, 6, 23, 30})));
        StringBytesMap ib2 = new StringBytesMap();
        ib2.put(word, new BytesRange(Compressor.compressVbWithoutMemory(new int[]{10, 50, 103, 200})));
        LightString[] words = new LightString[] {word, mir};

        StringBytesMap.join(words, new StringBytesMap[] {ib1, ib2});
    }
}