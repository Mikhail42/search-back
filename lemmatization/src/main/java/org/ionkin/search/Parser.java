package org.ionkin.search;

import org.ionkin.search.map.BytesBytesMap;
import org.ionkin.search.map.StringStringMap;
import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // № any       init     type  ??  info                                           ??  ??           ? ?
    // 85 балтского	балтский ADJ	_   Case=Gen|Degree=Pos|Gender=Masc|Number=Sing	86	amod	      _	_
    // 86 xcgpu	    xcgpu	 X   	_   Foreign=Yes	                                   84	flat:foreign  _	_

    // select (any, init, info) from rows where russian start with digit
    private static final Pattern russian =
            Pattern.compile("\\d+\\s(.*)\\s(.*)\\s+(NOUN|VERB|ADJ|PUNCT|PRON|ADV|NUM|X)\\s(.*)\\s+.*");
    private static final Pattern english =
            Pattern.compile("\\d+\\s(.*)\\s(.*)\\s+(NOUN|VERB|ADJ|PUNCT|PRON|ADV|NUM|X)\\s.*");

    public static void main(String... args) throws Exception {
        if (!new File(Util.enLemmPath).exists()) {
            writeEnglish(Util.basePath + "englishLemms0531"); // TODO
        }
        if (!new File(Util.ruLemmPath).exists()) {
            writeRussian(null); // TODO
        }
        if (!new File(Util.wordLemmPath).exists()) {
            joinEnRu();
        }
        lemmFixer();
        /*StringStringMap common = new StringStringMap(wordLemmPath);
        StringStringMap fix = new StringStringMap();
        common.forEach((k, v) -> {
            if (k.startWith('-')) {
                fix.put(k.substring(1), v.substring(1));
            } else {
                fix.put(k, v);
            }
        });
        fix.write(Util.basePath + "fixWordMap.chmss");*/
    }

    public static void joinEnRu() throws IOException {
        StringStringMap enWordMap = new StringStringMap(Util.enLemmPath);
        StringStringMap ruWordMap = new StringStringMap(Util.ruLemmPath);
        StringStringMap common = ruWordMap;
        common.putAll(enWordMap);
        common.write(Util.wordLemmPath);
    }

    private static void lemmFixer() throws IOException {
        StringStringMap common = new StringStringMap(Util.basePath + "lemm/allWordMap — копия.chmss");
        StringStringMap res = new StringStringMap();
        common.forEach((k, v) -> {
            if (k.jakar(v) > 0.3) {
                if (k.startWith('-')) {
                    res.put(k.substring(1), v.substring(1));
                } else {
                    res.put(k, v);
                }
            }
        });
        res.write(Util.wordLemmPath);
    }

    public static void writeEnglish(String enFilename) throws IOException {
        int nThreads = 4;
        String body = read(enFilename);
        String[] four = new String[nThreads];
        int[] ends = new int[nThreads];
        for (int i = 0; i < nThreads; i++) {
            ends[i] = body.indexOf('\n', body.length() / nThreads * (i + 1));
            four[i] = body.substring((i == 0) ? 0 : ends[i - 1], ends[i]);
        }
        body = null;
        String pref = "enLemm";

        ParallelFor.par((i) -> {
            Matcher m = english.matcher(four[i]);
            BytesBytesMap wordMap = new BytesBytesMap();
            while (m.find()) {
                String any = m.group(1).trim();
                String init = m.group(2).trim();
                if (!any.equals(init)) {
                    wordMap.put(toBytes(any), toBytes(init));
                }
            }
            wordMap.write(Util.basePath + pref + i + ".chmbb");
        }, 0, nThreads);
        StringStringMap[] wordMaps = new StringStringMap[nThreads];
        for (int i = 0; i < nThreads; i++) {
            final int i0 = i;
            BytesBytesMap bbm = new BytesBytesMap(Util.basePath + pref + i + ".chmbb");
            wordMaps[i0] = new StringStringMap();
            bbm.forEach((k, v) -> wordMaps[i0].put(new LightString(k), new LightString(v)));
        }
        StringStringMap enWordMap = StringStringMap.join(wordMaps);
        enWordMap.write(Util.enLemmPath);
    }

    public static void writeRussian(String filename) throws Exception {
        int nThreads = 4;
        String body = read(filename);
        String[] four = new String[nThreads];
        int[] ends = new int[nThreads];
        for (int i = 0; i < nThreads; i++) {
            ends[i] = body.indexOf('\n', body.length() / nThreads * (i + 1));
            four[i] = body.substring((i == 0) ? 0 : ends[i - 1], ends[i]);
        }
        body = null;

        ParallelFor.par((i) -> {
            Matcher m = russian.matcher(four[i]);
            CompactHashSet<LightString> foreignSet = new CompactHashSet<>(new StringTranslator());
            BytesBytesMap wordMap = new BytesBytesMap();
            while (m.find()) {
                String any = m.group(1).trim();
                String init = m.group(2).trim();
                String info = m.group(4);
                if (!any.equals(init)) {
                    wordMap.put(toBytes(any), toBytes(init));
                }
                if (info.startsWith("_\tForeign=Yes")) {
                    foreignSet.add(new LightString(toBytes(any)));
                }
            }

            wordMap.write("lemm" + i + ".chmbb");
            foreignSet.write("foreign" + i + ".chsls");
        }, 0, nThreads);
        StringStringMap[] wordMaps = new StringStringMap[nThreads];
        for (int i = 0; i < nThreads; i++) {
            final int i0 = i;
            BytesBytesMap bbm = new BytesBytesMap("lemm" + i + ".chmbb");
            wordMaps[i0] = new StringStringMap();
            bbm.forEach((k, v) -> wordMaps[i0].put(new LightString(k), new LightString(v)));
        }
        StringStringMap russianWordMap = StringStringMap.join(wordMaps);
        russianWordMap.write(Util.ruLemmPath);

        CompactHashSet<LightString> foreignSet = new CompactHashSet<>(new StringTranslator());
        for (int i = 0; i < nThreads; i++) {
            foreignSet.addAll(CompactHashSet.read("foreign" + i + ".chsls", new StringTranslator()));
        }
        foreignSet.write("foreign.chsls");
    }


    private static byte[] toBytes(String s) {
        s = normalize(s);
        return new LightString(s).getBytes();
    }

    public static final Locale RU = new Locale("RU");

    public static String normalize(String s) {
        s = s.toLowerCase(RU);
        s = s.replace('ё', 'е');
        s = s.replace("\u0301", "");
        return s;
    }

    public static String read(String filename) throws IOException {
        return new String(IO.read(filename), StandardCharsets.UTF_8);
    }
}
