package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringBytesTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws IOException {
        logger.info("start");
        EvaluatorFactory evaluatorFactory = new EvaluatorFactory();
        evaluatorFactory.init();
        logger.debug("init successfully");
        CompactHashMap<LightString, byte[]> map = evaluatorFactory.getIndex();
        Serializer.serialize(map, "serialized");
        //  map.write("fastMap");
      //  map = null;
       // CompactHashMap<LightString, byte[]> map2 = CompactHashMap.read("fastMap", new StringBytesTranslator());
       /* long sumLength = 0;
        int count = 0;
        for (LightString ls : index.keySet()) {
            byte[] bytes = index.get(ls);
            int c = Compressor.decompressedLength(bytes);
            sumLength += ls.length() * c;
            count += c;
        }
        sumLength /= count;
        System.err.println(sumLength);*/
        /*Evaluator evaluator = evaluatorFactory.create();
        String query = "!word && (a || b) && c || p";
        int[] result = evaluator.evaluate(query);
        for (int r : result) {
            logger.debug("{}", r);
        }*/
    }

    private static int[] readIds(String filename) throws IOException {
        byte[] comp = IO.read(filename);
        byte[] sums = Compressor.decompressAndSumInts(comp);
        return IO.readArrayInt(sums, 0, sums.length / 4);
    }
}
