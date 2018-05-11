package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.IntIntTranslator;
import org.ionkin.search.map.StringBytesTranslator;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.ionkin.search.FileWorker.articlesText;

public class IndexWorker {
    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);
    static final String basePath = "/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/";

    public static void main(String... args) throws Exception {
        logger.debug("started");
        //File f = new File("/media/mikhail/Windows/Users/Misha/workspace/booleanevalparser/target/boolean-eval-parser-1.0.jar");
        //System.err.println(f.toURI().toURL());
        //Map<Integer, Integer> idPositionMap =
        //      CompactHashMap.read(Util.basePath + "articlePositionsCompact", new IntIntTranslator());
    }

    private static void compressIndex() throws Exception {
        CompactHashMap<LightString, byte[]> index =
                CompactHashMap.read(Util.basePath + "joined1000Index/0", new StringBytesTranslator());
        logger.debug("big map is readed");
        AtomicInteger integer = new AtomicInteger();
        index.replaceAll((k, v) -> {
            if (integer.incrementAndGet() % 1000 == 0) {
                logger.debug("count: {}", integer.get());
            }
            byte[] diff = Compressor.diffInts(v);
            return Compressor.compressInts(diff);
        });
        logger.debug("compress successfully");
        index.write(Util.basePath + "joined1000Index/compressed");
    }

    private static void checkIndex() throws Exception {
        CompactHashMap<LightString, byte[]> index =
                CompactHashMap.read(Util.basePath + "joined1000Index/0", new StringBytesTranslator());
        logger.debug("big map is readed");
        index.forEach((k, v) -> {
            for (byte b : k.getBytes()) {
                if (((b < '0' || b > 'z') || (b > (':' + 32) && b < 'a')) && (b != '-')) {
                    throw new IllegalStateException("state: " + b);
                }
            }
        });
    }

    public static void sfds() throws Exception {
        byte[] bytes = IO.read(basePath + "ids/allDocumentId");
        int[] ar = IO.readArrayInt(bytes, 0, bytes.length / 4);
        CompactHashMap<Integer, Integer> idEndPositionMap =
                CompactHashMap.read(basePath + "ids/endPositions", new IntIntTranslator());
        CompactHashSet<LightString> tokens = new CompactHashSet<>(new StringTranslator());
        Function<Integer, String> f = articlesText(idEndPositionMap);
        for (int id : ar) {
            String text = f.apply(id);
            Iterable<String> strs = Util.splitPattern.split(text);
            for (String s : strs) {
                String normal = Util.normalize(s);
                if (Util.searchable(normal)) {
                    tokens.add(new LightString(normal));
                }
            }
        }
        tokens.write("tokens");
    }
}
   /*
   private static void finalJoin() throws Exception {
        logger.info("start final join");
        Map<LightString, byte[]> part0Compressed =
                // TODO
                deserializeMapOfBytes(Util.basePath + "joined100Compressed/" + 0);
        for (int i = 1; i < 8; i++) {
            Map<LightString, byte[]> iPartCompressed =
                    deserializeMapOfBytes(Util.basePath + "joined100Compressed/" + i);
            iPartCompressed.forEach((k, v) -> {
                if (!part0Compressed.containsKey(k)) {
                    part0Compressed.put(k, v);
                } else {
                    try {
                        byte[] a1 = decompress(part0Compressed.get(k));
                        byte[] a2 = decompress(iPartCompressed.get(k));

                        byte[] a = new byte[a1.length + a2.length];
                        System.arraycopy(a1, 0, a, 0, a1.length);
                        System.arraycopy(a2, 0, a, a1.length, a2.length);

                        byte[] compressed = Compressor.compress(a);
                        part0Compressed.put(k, compressed);
                    } catch (IOException e) {

                        logger.warn("w", e);
                    }
                }
            });
            logger.debug("map0 joined with map{}", i);
        }
        serializeMapOfBytes(part0Compressed, Util.basePath + "joinedAllCompressed/" + 0);
        logger.debug("finish!");
    }
   private static void compressJoined100(int from) throws Exception {
        for (int i = from; i < 8; i++) {
            Map<LightString, int[]> lightStringMap =
                    deserializeMapStrIntAr(Util.basePath + "joined100Index/" + i);
            logger.debug("deserialize successfully. size: {}", lightStringMap.size());
            Map<LightString, byte[]> compressed = new HashMap<>(lightStringMap.size() * 2);
            lightStringMap.forEach((k, v) -> {
                try {
                    compressed.put(k, Compressor.compress(toBytes(v)));
                } catch (IOException ioe) {
                    logger.warn("can't compress k=" + k, ioe);
                }
            });
            logger.debug("compressed successfully");
            serializeMapOfBytes(compressed, Util.basePath + "joined100Compressed/" + i);
        }
    }
   private static void join1000() throws Exception {
        CompactHashMap<LightString, byte[]> index = new CompactHashMap<>(new StringBytesTranslator());
        logger.debug("big map is created");
        for (int i = 0; i < 8; i++) {
            Map<LightString, int[]> local = deserializeMapStrIntAr(Util.basePath + "joined100Index/" + i);
            logger.debug("local map is readed. size: {}", local.size());
            local.forEach((k, v0) -> {
                byte[] v = IO.toBytes(v0);
                if (index.containsKey(k)) {
                    byte[] ar1 = index.get(k);
                    byte[] a = new byte[ar1.length + v.length];
                    System.arraycopy(ar1, 0, a, 0, ar1.length);
                    System.arraycopy(v, 0, a, ar1.length, v.length);
                    index.put(k, a);
                    ar1 = null;
                    a = null;
                } else {
                    index.put(k, v);
                }
                k = null;
                v = null;
                v0 = null;
            });
            logger.debug("local map is writed to index. index size: {}", index.size());
            local = null;
            System.gc();
            logger.debug("after gc");
            index.write(Util.basePath + "joined1000Index/" + i + 1);
            logger.debug("after write");
        }
        logger.debug("joined successfully");
        index.write(Util.basePath + "joined1000Index/" + 0);
    }
    private static void join100(int from) throws Exception {
        for (int dec = from; dec < 7; dec++) {
            //int dec = 0;
            HashMap<LightString, List<int[]>> index = new HashMap<>();
            for (int i = dec * 10; i < (dec + 1) * 10; i++) {
                Map<LightString, int[]> local = deserializeMapStrIntAr(Util.basePath + "joined10Index/" + i);
                local.forEach((k, v) -> {
                    List<int[]> list = index.getOrDefault(k, new LinkedList<>());
                    list.add(local.get(k));
                    index.put(k, list);
                });
            }
            logger.debug("joined successfully");
            serializeMapOfList(index, Util.basePath + "joined100Index/" + dec);
        }
    }

   private static void join10() throws Exception {
        for (int dec = 0; dec < 73; dec++) {
            HashMap<LightString, List<int[]>> index = new HashMap<>();
            for (int i = dec * 10; i < (dec + 1) * 10; i++) {
                Map<LightString, int[]> local = deserializeMapStrIntAr(Util.basePath + "invertedIndex/" + i);
                local.forEach((k, v) -> {
                    List<int[]> list = index.getOrDefault(k, new LinkedList<>());
                    list.add(local.get(k));
                    index.put(k, list);
                });
            }
            logger.debug("joined successfully");
            serializeMapOfList(index, Util.basePath + "joined10Index/" + dec);
        }
    }
   private static void getSetStrings() throws Exception {
        //Map<LightString, Integer> s =
        for (int i = 0; i < 8; i++) {
            Map<LightString, int[]> lightStringMap =
                    deserializeMapStrIntAr(Util.basePath + "joined100Index/" + i);
            logger.debug("deserialize successfully. size: {}", lightStringMap.size());
            lightStringMap.forEach((k, v) -> map.put());
        }
        logger.debug("size: {}", set.size());
        long size = 0;
        for (LightString s : set) {
            size += s.length();
        }
        logger.debug("size at bytes: {}", size);
    }*/