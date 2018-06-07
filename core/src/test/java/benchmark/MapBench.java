package benchmark;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapBench {

    public static void main(String... args) throws IOException {
        String text = "t ext ss \n <doc";
        Pattern pattern = Pattern.compile("(.*?(?=<doc))<doc", Pattern.DOTALL);
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            System.out.println( m.group(1));
        }

        /*String pat = "<doc id=\"([\\d]{1,})\" title=\"([^\"&>]{1,})\">(.{1,})</doc>";
        Pattern pattern = Pattern.compile(pat);
        String s = "<doc id=\"7\" title=\"Литва\">d text</doc>\n" +
                "<doc id=\"9\" title=\"Россия\">Россия</doc>\n" +
                "<doc id=\"15\" title=\"Красная книга\">Красная книга</doc>";

        Matcher m = pattern.matcher(s);
        while (m.find()) {
            int id = Integer.parseInt(m.group(1));
            String title = m.group(2);
            String text = m.group(3);
            System.err.println(text);
        }*/


        /*long s = System.currentTimeMillis();
        String filename = "/home/mikhail/pos50000/4";
        CompactHashMap<LightString, CompactHashMap<Integer, byte[]>> map =
                CompactHashMap.deserialize(IO.read(filename), new StringPositionsTranslator());
// TODO
        AtomicLong keyLength = new AtomicLong(0);
        AtomicLong valueLength = new AtomicLong(0);
        AtomicLong indexLength = new AtomicLong(0);
        AtomicLong posLength = new AtomicLong(0);
        AtomicLong indexSize = new AtomicLong(0);

        map.forEach((k, v) -> {
            keyLength.addAndGet(k.length());
            keyLength.addAndGet(VariableByte.compressedLength(k.length()));

            long size = v.sizeOfTableWithLength();
            valueLength.addAndGet(size);

            v.forEach((vk, vv) -> {
                indexLength.addAndGet(VariableByte.compressedLength(vk));
                posLength.addAndGet(vv.length);
            });
            indexSize.addAndGet(v.size());
        });
        System.err.println("" + keyLength + ", "  + valueLength + ", " + indexLength + ", " + posLength + ", " + indexSize);
        System.err.println(System.currentTimeMillis() - s);
        */
        // IO.write(mas, filename+"comp");
    }
}
