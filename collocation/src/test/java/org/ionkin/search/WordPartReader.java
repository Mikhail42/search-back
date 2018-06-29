package org.ionkin.search;

import com.google.common.base.Splitter;
import org.ionkin.search.map.StringStringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordPartReader {
    private static final Logger logger = LoggerFactory.getLogger(WordPartReader.class);

    public static void main(String... args) throws IOException {
        StringStringMap ssm = new StringStringMap();

        String filename = "C:\\Users\\Misha\\workspace\\wiki-bz2\\lemm\\lemms0530";
        try (final FileChannel readChannel = new RandomAccessFile(filename, "r").getChannel()) {
            final long fileLength0 = readChannel.size();
            logger.info("read from {}. fileLength0={}", filename, fileLength0);
            if (fileLength0 < Integer.MAX_VALUE) {
                ByteBuffer readBuffer = readChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileLength0);
                byte[] bytes = new byte[(int) fileLength0];
                readBuffer.get(bytes);
                String s = new String(bytes, StandardCharsets.UTF_8);
                readBuffer = null;
                bytes = null;
                Pattern pattern = Pattern.compile("^\\d+\\s+[\\p{L}p{N}-]+\\s+([\\p{L}p{N}-]+)\\s+([\\p{L}p{N}-]+)\\s+.*");
                final Splitter splitPatternLazy = Splitter.onPattern("\n");
                splitPatternLazy.split(s).forEach(str -> {
                    Matcher m = pattern.matcher(str);
                    if (m.find()) {
                        ssm.putIfAbsent(new LightString(m.group(1)), new LightString(m.group(2).toLowerCase()));
                    }
                });
            }
        }

        ssm.write(Util.basePath + "language-part.ssm");
    }
}
