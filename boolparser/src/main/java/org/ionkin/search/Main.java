package org.ionkin.search;

import com.google.common.base.Utf8;
import javafx.util.Pair;
import org.ionkin.search.map.*;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {

    }

    private static void quoteTest() {
        /*EvaluatorPerformance evaluator = load(Util.basePath + "test.index", Util.basePath + "test.posit");

        long t1 = System.currentTimeMillis();
        //logger.info(Arrays.toString(evaluator.evaluateDocIds("сколько ног у многоножки", 10)));
        List<Pair<Integer, String>> snipps = evaluator.evaluate("«джек воробей»", 50);
        snipps.forEach(pair -> {
            logger.info("{} {}", pair.getKey(), pair.getValue());
        });
        logger.info(Arrays.toString(evaluator.evaluate("«об авторских правах»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Слово о полку Игореве»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«война и мир»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«1 российский фильм»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Петр Великий»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Двенадцать стульев»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«Булев поиск»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate(" «что  где  когда»  &&  !«хрустальная  сова»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»  /  5", 50)));
        logger.info(Arrays.toString(evaluator.evaluate("«что  где  когда»    &&    друзь", 50)));
        logger.info(Arrays.toString(evaluator.evaluate(" «что  где  когда»  ||    квн", 50)));

        logger.info("middle time: {}", (System.currentTimeMillis() - t1) / 11);*/
    }

    private static void csv() throws Exception {
        String[] qs = ("1 список лучших фильмов\n" +
                "2 тесла в космосе\n" +
                "3 что такое хорошо и что такое плохо\n" +
                "4 адвокат дьявола\n" +
                "5 сколько ног у многоножки\n" +
                "6 игра престолов список серий\n" +
                "7 451 по фаренгейту\n" +
                "8 министерство правды" + "\n" +
                "9 когда была парижская коммуна\n" +
                "10 когда вымерли динозавры\n" +
                "11 игра поле чудес\n" +
                "12 джек воробей\n" +
                "13 наследники британского престола\n" +
                "14 цветик семицветик\n" +
                "15 где расположена литва\n" +
                "16 россия на евровидении\n" +
                "17 крымский мост\n" +
                "18 я помню чудное мгновенье\n" +
                "19 кто такой нельсон мандела\n" +
                "20 как называют жителей набережных челнов\n" +
                "21 роза ветров\n" +
                "22 торт павлова\n" +
                "23 автор повести временных лет\n" +
                "24 редкие животные\n" +
                "25 текст гимна россии\n" +
                "26 нос гоголя\n" +
                "27 когда день рождения путина\n" +
                "28 винни пух и все все все\n" +
                "29 все псы попадают в рай\n" +
                "30 сколько ждал хатико").split("\n");
        Pattern p = Pattern.compile("\\d+ (.*)");
        for (int i = 0; i < qs.length; i++) {
            Matcher m = p.matcher(qs[i]);
            if (m.find()) {
                qs[i] = m.group(1);
            }
        }

        EvaluatorPerformance evaluator = EvaluatorPerformance.loadTest();
        StringBuffer sb = new StringBuffer();

        for (String q : qs) {
            try {
                int[] docIds = evaluator.evaluate(q, 10).stream().mapToInt(Pair::getKey).toArray();
                if (docIds.length > 0) {
                    sb.append("\"" + q + "\",1,\"https://ru.wikipedia.org/?curid=" + docIds[0] + "\",0\n");
                }
                for (int i = 1; i < docIds.length; i++) {
                    sb.append("\"\"," + (i + 1) + ",\"https://ru.wikipedia.org/?curid=" + docIds[i] + "\",0\n");
                }
            } catch (Exception e) {
                logger.error("er: ", e);
            }
        }

        logger.info(sb.toString());
    }

    private static void loadWithTest(String indexPath, String positionsPath) throws Exception {
        StringBytesMap sbMap = new StringBytesMap(indexPath);
        IndexMap indexMap = new IndexMap(sbMap);

        StringPositionsMap spMap = new StringPositionsMap(positionsPath);
        SearchMap searchMap = new SearchMap(spMap);

        LightString war = new LightString("война");
        int[] warIds = Compressor.decompressVb(sbMap.get(war));
        for (int id : warIds) {
            logger.info("id: {}", id);
            IntBytesMap ibm = spMap.get(war);
            BytesRange br = ibm.get(id);
            int[] ar1 = Compressor.decompressVb(br);
            /*if (id == 256) {
                int a = 5;
            }*/
            int[] ar2 = Compressor.decompressVb(searchMap.get(war).positions(id));
            boolean f = Arrays.equals(ar1, ar2);
            logger.info("id: {}", f);
        }
        logger.info(Arrays.toString(Compressor.decompressVb(spMap.get(war).get(9))));
        logger.info(Arrays.toString(Compressor.decompressVb(spMap.get(war).get(6040))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(7))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(7))));
        logger.info(Arrays.toString(Compressor.decompressVb(searchMap.get(war).positions(9))));
    }

    private static void speedTest(StringPositionsMap spMap) {
        AtomicLong totalPos = new AtomicLong();
        AtomicLong totalDocIds = new AtomicLong();
        spMap.forEach((k, v) -> {
            v.forEach((did, pos) -> {
                int n = 0;
                for (int i = 0; i < pos.length(); i++) n += pos.get(i) < 0 ? 1 : 0;
                totalPos.addAndGet(n);
                if (n == 0) throw new RuntimeException("pos length is zero: k=" + k + " " + did);
                totalDocIds.addAndGet(1);
            });
        });
        logger.info("TOTAL POS: {}", totalPos.get());
        logger.info("KEY-DOCID PAIRS: {}", totalDocIds.get());
        logger.info("SIZE: {}", spMap.size());
    }

    private static void writeTestMap() throws Exception {
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);
        Stream<LightString> words = Stream.of("сколько ждал хатико".split("\\s+"))
                .map(Normalizer::normalize).map(LightString::new)
                .map(w -> lemms.getOrDefault(w, w));

        StringBytesMap indexMap = new StringBytesMap(Util.basePath + "indexlemm.sbm");
        StringBytesMap testIm = new StringBytesMap();
        StringPositionsMap searchMap = new StringPositionsMap(Util.basePath + "positionslemm.spm");
        StringPositionsMap testSm = new StringPositionsMap();

        words.forEach(w -> {
            testSm.put(w, searchMap.get(w));
            testIm.put(w, indexMap.get(w));
        });

        testIm.write(Util.basePath + "test.index");
        testSm.write(Util.basePath + "test.posit");
    }

    @Deprecated
    private static void writeTestMaps() throws Exception {
        StringStringMap lemms = new StringStringMap(Util.wordLemmPath);
        List<LightString> words =
                Stream.of("сколько ждал хатико".split(" "))
                        .map(LightString::new).map(w -> lemms.getOrDefault(w, w))
                        .collect(Collectors.toList());

        StringPositionsMap spMap = new StringPositionsMap(Util.positionsPath);
        StringPositionsMap testSearch = new StringPositionsMap();
        words.forEach(w -> testSearch.put(w, spMap.get(w)));
        testSearch.write(Util.testPositionsPath);

        StringBytesMap indexMap = new StringBytesMap(Util.testIndexPath);
        StringBytesMap testIndexMap = new StringBytesMap();
        words.forEach(w -> testIndexMap.put(w, indexMap.get(w)));
        testIndexMap.write(Util.testIndexPath);
    }


    private static void write(String inputFile, String outFile) throws IOException {
        CompactHashSet<LightString> tokensMap =
                CompactHashSet.read(inputFile, new StringTranslator());
        logger.info("token set are read");
        LightString[] tokens = Util.toArray(tokensMap);
        tokensMap = null;
        int size = 0;
        byte[] space = " ".getBytes(StandardCharsets.UTF_8);
        String[] toks = new String[tokens.length];
        for (int i=0; i<tokens.length; i++) {
            toks[i] = tokens[i].asString();
            size += Utf8.encodedLength(toks[i]);
            size += space.length;
            tokens[i] = null;
        }
        tokens = null;
        logger.info("size=" + size);
        try (FileChannel rwChannel = new RandomAccessFile(outFile, "rw").getChannel()) {
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            for (String tok: toks) {
                byte[] ar = tok.getBytes(StandardCharsets.UTF_8);
                wrBuf.put(ar);
                wrBuf.put(space);
            }
        }
    }
}
