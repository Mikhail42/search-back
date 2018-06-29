package org.ionkin.search;

import org.junit.Before;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeanStdTest {

    private MeanStd meanStd;

    @Before
    public void before() throws IOException {
        meanStd = new MeanStd();
    }

    //@Test
    public void meanStd() {
        String[] ar = new String[]{
                "молоко", "ананас", "апельсин", "он", "ты", "белка", "дорога", "человек", "ноутбук", "язык", "тело"
        };
        LightString[] ls = Stream.of(ar).map(LightString::new).collect(Collectors.toList()).toArray(new LightString[0]);

        HashMap<Integer, ByteArray> res = meanStd.wordIdWindowIds(ls);
        HashMap<String, int[]> r2 = new HashMap<>();
        res.forEach((k, v) -> r2.put(
                meanStd.isRelat.get(k).asString(),
                VariableByte.uncompress(v.getCopy(), new IntWrapper(0), 100)));
        int a = 5;
    }
}
