package org.ionkin.search;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectIndex {
    private static final Logger logger = LoggerFactory.getLogger(DirectIndex.class);

    public static final String OUT_FILENAME =
            "/media/mikhail/Windows/Users/Misha/Downloads/ruwiki-20180201-pages-articles-multistream-index.txt/arrayTitles";
    public static final int ROWS = 7_400_000;

    public static void main(String... args) throws Exception {
        logger.info("start");
        write(OUT_FILENAME);
        /*String[] tits = read(OUT_FILENAME);
        for (int i = 0; i < 100; i++) {
            logger.debug(tits[i]);
        }*/
        logger.info("stop");
    }

    public static void write(String outFilename) throws IOException {
        String filename = "/media/mikhail/Windows/Users/Misha/Downloads/ruwiki-20180201-pages-articles-multistream-index.txt" +
                "/ruwiki-20180201-pages-articles-multistream-index.txt";
        byte[] bytes = IO.read(filename);
        String file = new String(bytes, StandardCharsets.UTF_8);
        logger.debug("string created from bytes");
        Iterable<String> lines = Splitter.on('\n').split(file);
        logger.debug("string splitted");
        int length = 0;
        String[] titles = new String[ROWS];
        Pattern pattern = Pattern.compile("[\\d]{1,}:([\\d]{1,}):(.*)");
        for (String line : lines) {
            if (line.length() > 0) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    String v = m.group(2);
                    titles[Integer.parseInt(m.group(1))] = v;
                    length += v.length() * 2 + 4;
                }
            }
        }
        lines = null;
        for (String title : titles) {
            if (title == null) {
                length += 4;
            }
        }
        byte[] res = new byte[length];
        int pos = 0;
        for (int i = 0; i < titles.length; i++) {
            if (titles[i] != null) {
                IO.putStringWithLength(res, titles[i], pos);
                pos += 4 + titles[i].length() * 2;
            } else {
                IO.putInt(res, 0, pos);
                pos += 4;
            }
        }
        IO.write(res, outFilename);
        logger.info("titles writed");
    }

    public static String[] read(String filename) throws IOException {
        logger.debug("start read titles");
        byte[] bytes = IO.read(filename);
        logger.trace("bytes read");
        String[] titles = new String[ROWS];
        logger.trace("array of titles created");
        int pos = 0;
        int i = 0;
        while (pos < bytes.length) {
            int length = IO.readInt(bytes, pos);
            pos += 4;
            if (length != 0) {
                titles[i] = readString(bytes, pos, length);
                pos += length * 2;
            }
            i++;
        }
        return titles;
    }

    public static String readString(byte[] ar, int pos, int length) {
        byte[] str = new byte[length * 2 + 2];
        str[0] = -2;
        str[1] = -1;
        System.arraycopy(ar, pos, str, 2, str.length - 2);
        return new String(str, StandardCharsets.UTF_16);
    }
}
