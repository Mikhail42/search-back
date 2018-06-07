package org.ionkin.search;

import com.google.common.base.Utf8;
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

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        byte[] ar = new byte[1_000_00];
        for (int i=0; i<ar.length; i++) {
            ar[i] = (byte) i;
        }
        long t1 = System.nanoTime();
        byte[] b2 = Arrays.copyOf(ar, ar.length);
        System.out.println(System.nanoTime() - t1);
        System.out.println(b2[105]);
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
