package org.ionkin.search.map;

import org.ionkin.search.Compressor;
import org.ionkin.search.LightString;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class CompactHashMapTest {

    private final CompactMapTranslator<LightString, byte[]> translator = new StringBytesTranslator();

    /**
     * @author M. Ionkin
     */
    @Test
    public void testWriteRead() throws IOException {
        File file = File.createTempFile("MyApp", "tmp");
        file.deleteOnExit();
        String filename = file.getAbsolutePath();

        CompactHashMap<LightString, byte[]> map = new CompactHashMap<>(translator);
        byte[] ar = new byte[]{1, 5, 9, 3, 2};
        map.put(new LightString("мама"), ar);
        map.write(filename);

        StringBytesMap map2 = new StringBytesMap();
        map2.read(filename);
        assertTrue(Arrays.equals(ar, map2.get(new LightString("мама"))));
    }

    @Test
    public void testPut() {
        CompactHashMap<String, Integer> map = new CompactHashMap<String, Integer>(TRANSLATOR);
        assertEquals(null, map.put("a", 9));
        assertEquals(null, map.put("b", 8));
        assertEquals(null, map.put("c", 7));
        assertEquals(null, map.put("d", 6));
        map.checkStructure();
        assertEquals(null, map.put("e", 5));
        assertEquals(null, map.put("f", 4));
        assertEquals(null, map.put("g", 3));
        assertEquals(null, map.put("h", 2));
        map.checkStructure();
        assertEquals((Integer) 9, map.put("a", 0));
        assertEquals((Integer) 8, map.put("b", 1));
        assertEquals((Integer) 7, map.put("c", 2));
        map.checkStructure();
        assertEquals((Integer) 6, map.put("d", 3));
        assertEquals((Integer) 5, map.put("e", 4));
        assertEquals((Integer) 4, map.put("f", 5));
        map.checkStructure();
        assertEquals((Integer) 3, map.put("g", 6));
        assertEquals((Integer) 2, map.put("h", 7));
        map.checkStructure();
    }

    /*
        @Test public void testSize() {
            CompactHashMap<String,Integer> map = new CompactHashMap<String,Integer>(TRANSLATOR);
            map.checkStructure();
            assertEquals(0, map.size());
            map.put("xy", 32);
            assertEquals(1, map.size());
            map.put("xyz", 27);
            assertEquals(2, map.size());
            assertEquals((Integer)32, map.put("xy", 5));
            assertEquals(2, map.size());
            map.put("a", 0);
            map.put("b", 1);
            map.put("c", -1);
            assertEquals(5, map.size());
            map.checkStructure();
        }


        @Test public void testMediumSimple() {
            Map<String,Integer> map = new CompactHashMap<String,Integer>(TRANSLATOR);
            for (int i = 0; i < 10000; i++) {
                assertNull(map.put(Integer.toString(i), i));
                assertEquals(i + 1, map.size());
                int j = rand.nextInt(20000) - 5000;
                assertEquals(j >= 0 && j <= i ? (Integer)j : null, map.get(Integer.toString(j)));
            }
        }


        @Test public void testMediumSeesaw() {
            Map<String,Integer> map0 = new HashMap<String,Integer>();
            CompactHashMap<String,Integer> map1 = new CompactHashMap<String,Integer>(TRANSLATOR);
            for (int i = 0; i < 30; i++) {
                // Generate random data
                int n = rand.nextInt(30000);
                String[] keys = new String[n];
                Integer[] values = new Integer[n];
                for (int j = 0; j < n; j++) {
                    keys[j] = Integer.toString(rand.nextInt(100000), 36);  // Can produce duplicates
                    values[j] = rand.nextInt();
                }

                // Do all insertions
                for (int j = 0; j < n; j++) {
                    assertEquals(map0.put(keys[j], values[j]), map1.put(keys[j], values[j]));
                    String query = Integer.toString(rand.nextInt(100000), 36);
                    assertTrue(map0.containsKey(query) == map1.containsKey(query));
                    assertEquals(map0.get(query), map1.get(query));
                    if (rand.nextDouble() < 0.001)
                        map1.checkStructure();
                }
                assertEquals(map0.size(), map1.size());  // May be less than n due to duplicate keys

                // Do all removals
                for (int j = 0; j < n; j++) {
                    assertEquals(map0.remove(keys[j]), map1.remove(keys[j]));
                    String query = Integer.toString(rand.nextInt(100000), 36);
                    assertTrue(map0.containsKey(query) == map1.containsKey(query));
                    assertEquals(map0.get(query), map1.get(query));
                    if (rand.nextDouble() < 0.001)
                        map1.checkStructure();
                }
                assertEquals(0, map0.size());
                assertEquals(0, map1.size());
            }
        }


        @Test public void testLargeRandomly() {
            Map<String,Integer> map0 = new HashMap<String,Integer>();
            CompactHashMap<String,Integer> map1 = new CompactHashMap<String,Integer>(TRANSLATOR);
            for (int i = 0; i < 1000000; i++) {
                String key = Integer.toString(rand.nextInt(100000), 36);
                int op = rand.nextInt(10);
                if (op < 5) {
                    int val = rand.nextInt();
                    assertEquals(map0.put(key, val), map1.put(key, val));
                } else {
                    assertEquals(map0.remove(key), map1.remove(key));
                }

                assertEquals(map0.size(), map1.size());
                String query = Integer.toString(rand.nextInt(100000), 36);
                assertTrue(map0.containsKey(query) == map1.containsKey(query));
                assertEquals(map0.get(query), map1.get(query));
                if (rand.nextDouble() < 0.0001)
                    map1.checkStructure();
            }
        }
    */
/*
    @Test public void testIteratorDump() {
        for (int i = 0; i < 100; i++) {
            // Generate random data
            int n = rand.nextInt(30000);
            String[] keys = new String[n];
            Integer[] values = new Integer[n];
            for (int j = 0; j < n; j++) {
                keys[j] = Integer.toString(rand.nextInt(100000), 36);  // Can produce duplicates
                values[j] = rand.nextInt();
            }

            // Do insertions and removals
            Map<String,Integer> map0 = new HashMap<String,Integer>();
            CompactHashMap<String,Integer> map1 = new CompactHashMap<String,Integer>(TRANSLATOR);
            for (int j = 0; j < n / 2; j++) {
                map0.put(keys[j], values[j]);
                map1.put(keys[j], values[j]);
            }
            for (int j = n / 2; j < n; j++) {
                map0.remove(keys[j]);
                map1.remove(keys[j]);
            }
            map1.checkStructure();

            // Test the iterator
            for (Map.Entry<String,Integer> entry : map1.entrySet())
                assertEquals(map0.remove(entry.getKey()), entry.getValue());
            assertEquals(0, map0.size());
        }
    }


    @Test public void testIteratorModifyRemove() {
        for (int i = 0; i < 100; i++) {
            // Generate random data
            int n = rand.nextInt(30000);
            String[] keys = new String[n];
            Integer[] values = new Integer[n];
            for (int j = 0; j < n; j++) {
                keys[j] = Integer.toString(rand.nextInt(100000), 36);  // Can produce duplicates
                values[j] = rand.nextInt();
            }

            // Do insertions and removals
            Map<String,Integer> map0 = new HashMap<String,Integer>();
            CompactHashMap<String,Integer> map1 = new CompactHashMap<String,Integer>(TRANSLATOR);
            for (int j = 0; j < n / 2; j++) {
                map0.put(keys[j], values[j]);
                map1.put(keys[j], values[j]);
            }
            for (int j = n / 2; j < n; j++) {
                map0.remove(keys[j]);
                map1.remove(keys[j]);
            }
            map1.checkStructure();

            // Do iterator removals and map entry modifications
            double deleteProb = rand.nextDouble();
            for (Iterator<Map.Entry<String,Integer>> iter = map1.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String,Integer> entry = iter.next();
                if (rand.nextDouble() < deleteProb) {
                    iter.remove();
                    map0.remove(entry.getKey());
                } else if (rand.nextDouble() < 0.2) {
                    int value = rand.nextInt();
                    entry.setValue(value);
                    map0.put(entry.getKey(), value);
                }
            }
            map1.checkStructure();
            assertEquals(map0.size(), map1.size());

            // Check remaining contents for sameness
            for (Map.Entry<String,Integer> entry : map1.entrySet())
                assertEquals(map0.remove(entry.getKey()), entry.getValue());
            assertEquals(0, map0.size());
        }
    }

    private static Random rand = new Random();

*/
    // Serialization format: (String s, int n) -> [s as bytes in UTF-8] + [n as 4 bytes in big endian].
    private static final CompactMapTranslator<String, Integer> TRANSLATOR = new CompactMapTranslator<String, Integer>() {

        public boolean isKeyInstance(Object obj) {
            return obj instanceof String;
        }


        public int getHash(String key) {
            int state = 0;
            for (int i = 0; i < key.length(); i++) {
                state += key.charAt(i);
                for (int j = 0; j < 4; j++) {
                    state *= 0x7C824F73;
                    state ^= 0x5C12FE83;
                    state = Integer.rotateLeft(state, 5);
                }
            }
            return state;
        }


        public byte[] serialize(String key, Integer value) {
            try {
                byte[] packed = key.getBytes("UTF-8");
                int off = packed.length;
                packed = Arrays.copyOf(packed, off + 4);
                int val = value;
                packed[off + 0] = (byte) (val >>> 24);
                packed[off + 1] = (byte) (val >>> 16);
                packed[off + 2] = (byte) (val >>> 8);
                packed[off + 3] = (byte) (val >>> 0);
                return packed;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }


        public String deserializeKey(byte[] packed) {
            try {
                return new String(packed, 0, packed.length - 4, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }


        public Integer deserializeValue(byte[] packed) {
            int n = packed.length;
            return (packed[n - 1] & 0xFF) | (packed[n - 2] & 0xFF) << 8 | (packed[n - 3] & 0xFF) << 16 | packed[n - 4] << 24;
        }
    };

    @Test
    public void sizeOfTableWithLength() {
        CompactHashMap<LightString, byte[]> map = new CompactHashMap<>(new StringBytesTranslator());
        map.put(new LightString("мама"), new byte[]{1, 2, 3});
        map.put(new LightString("папа"), new byte[]{1, 2, 3, 5});
        assertEquals( (4 + 1 + 1 + 3) + (4 + 1 + 1 + 4), map.sizeOfTableWithLength());
    }

    public LightString[] toArray(Set<LightString> set) {
        LightString[] a = new LightString[set.size()];
        int i = 0;
        for (LightString val : set) a[i++] = val;
        return a;
    }

    @Test
    public void join() throws IOException {
        /*CompactHashMap<LightString, Integer> tokensMap =
                CompactHashMap.read("/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/freq/allTokens", new StringIntTranslator());
        LightString[] tokens = toArray(tokensMap.keySet());
        tokensMap = null;*/
        CompactHashMap<Integer, byte[]> ib1 = new CompactHashMap<>(new IntBytesTranslator());
        ib1.put(4, new byte[]{1, 5, 3, 2});
        ib1.put(6, new byte[]{1, 6, 23, 2});
        CompactHashMap<Integer, byte[]> ib2 = new CompactHashMap<>(new IntBytesTranslator());
        ib2.put(14, new byte[]{1, 5, 3, 2});

        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map1 = new CompactHashMap<>(new StringPositionsMapTranslator());
        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map2 = new CompactHashMap<>(new StringPositionsMapTranslator());
        map1.put(new LightString("word"), ib1);
        map2.put(new LightString("mir"), ib2);

        LightString[] tokens = new LightString[]{new LightString("word"), new LightString("mir"), new LightString("mir2")};

        StringPositionsMap res = StringPositionsMap.join(tokens, new CompactHashMap[]{map1, map2});
        res.forEach((k, v) -> {
            System.err.println(k);
            v.forEach((vk, vv) -> System.err.append(vk.toString()).append(' '));
            System.err.append('\n');
        });
        System.err.flush();
        int s = 4;
    }


    /**
     * @author M. Ionkin
     */
    @Test
    public void joinStringBytes() throws Exception {
        LightString word = new LightString("word");
        LightString mir = new LightString("mir");
        CompactHashMap<LightString, byte[]> ib1 = new CompactHashMap<>(new StringBytesTranslator());
        ib1.put(word, Compressor.compressVbWithoutMemory(new int[]{1, 3, 5, 6}));
        ib1.put(mir, Compressor.compressVbWithoutMemory(new int[]{1, 6, 23, 30}));
        CompactHashMap<LightString, byte[]> ib2 = new CompactHashMap<>(new StringBytesTranslator());
        ib2.put(word, Compressor.compressVbWithoutMemory(new int[]{10, 50, 103, 200}));
        LightString[] words = new LightString[] {word, mir};

        StringBytesMap.join(words, new CompactHashMap[] {ib1, ib2});
    }
}