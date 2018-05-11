package org.ionkin.search;

import junit.framework.TestCase;
import org.ionkin.search.map.CompactHashMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class EvaluatorFactoryTest extends TestCase {

    public void testAll() throws Exception {
        EvaluatorFactory factory = new EvaluatorFactory();
        factory.init();
        testIndex(factory);
        testGetFileIds(factory);
        testGetIdPositionMap(factory);
        testGetIdTitleMap(factory);
    }

    public void testIndex(EvaluatorFactory factory) throws Exception {
        CompactHashMap<LightString, byte[]> index = factory.getIndex();
        long sumLength = 0;
        Set<LightString> keySet = index.keySet();
        for (LightString key : keySet) {
            sumLength += key.length();
        }
        System.err.println(sumLength);
        System.err.println(keySet.size());
    }

    public void testGetIdTitleMap(EvaluatorFactory factory) throws Exception {
        String[] idTitleMap = factory.getTitles();
        assertEquals(idTitleMap[4], "Базовая статья");
        assertEquals(idTitleMap[7], "Литва");
        assertEquals(idTitleMap[9], "Россия");
        assertEquals(idTitleMap[10], "Слоновые");
        assertEquals(idTitleMap[11], "Мамонты");
        assertEquals(idTitleMap[15], "Красная книга");
        assertEquals(idTitleMap[16], "Соционика");
        assertEquals(idTitleMap[18], "Школа");
    }

    public void testGetFileIds(EvaluatorFactory factory) throws Exception {
        // EvaluatorFactory.writeIds(EvaluatorFactory.basePath + "ids/fileIds");
        int[] fileIds = factory.getFileIds();
        assertEquals(fileIds[0], 4);
        assertEquals(fileIds[1], 748);
        assertEquals(fileIds[2], 1483);
        assertEquals(fileIds[3], 2798);
        assertEquals(fileIds[4], 3673);
        assertEquals(fileIds[5], 4428);
        assertEquals(fileIds[6], 5028);
    }

    public void testGetIdPositionMap(EvaluatorFactory factory) throws Exception {
        CompactHashMap<Integer, Integer> idPosMap = factory.getIdEndPositionMap();
        idPosMap.forEach((k, v) -> {
            if (k < 100) {
                System.err.println("" + k + " " + v);
            }
        });
        // #REDIRECT [[Заглавная страница]]
        assertEquals(idPosMap.get(4), new Integer(12 + (9 + 8) * 2 + 1 + 2));
        assertTrue(idPosMap.get(7) > idPosMap.get(4));
        assertTrue(idPosMap.get(9) > idPosMap.get(7));
        assertTrue(idPosMap.get(10) > idPosMap.get(9));
        assertTrue(idPosMap.get(11) > idPosMap.get(10));
        assertTrue(idPosMap.get(15) > idPosMap.get(11));
        assertTrue(idPosMap.get(16) > idPosMap.get(15));
        assertTrue(idPosMap.get(18) > idPosMap.get(16));
    }

    public void testGetText() throws IOException {
        int id = 4;
        int s = 13248450;
        int e = 13570475;
        String art = new String(IO.read(EvaluatorFactory.basePath + id, s, e - s + 1), StandardCharsets.UTF_8);
        System.err.println(art);
    }
}