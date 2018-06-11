package org.ionkin.search;

import com.google.common.base.Splitter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {
    public static final String basePath = "C:/Users/Misha/workspace/wiki-bz2/";
    public static final String textPath = basePath + "text/";
    public static final String indexFolder = basePath + "index/";
    public static final String positionIndexFolder = basePath + "posindex/";
    public static final String indexPath = basePath + "index.chmsb";
    public static final String testIndexPath = basePath + "testIndex.chmsb";
    public static final String positionsPath = basePath + "positions.chmsp";
    public static final String testPositionsPath = basePath + "testPositions.chmsp";
    public static final String dictionaryPath = basePath + "allwords0606.chsls";
    public static final String wordLemmPath = basePath + "lemm/allWordMap.chmss";

    public static final String wordSymbol = "\\p{L}\\p{N}\u0301";
    public static final Splitter splitPatternLazy = Splitter.onPattern("[^" + wordSymbol + "]+");
    public static final Pattern splitPattern = Pattern.compile("[^" + wordSymbol + "]+");
    public static final Pattern wordPattern = Pattern.compile("[" + wordSymbol + "]+");

    public static final Locale RU = new Locale("RU");

    public static Set<LightString> lightStrings(String pageContent) {
        Iterable<String> words = splitPatternLazy.split(pageContent);
        Set<LightString> lightStrings = new HashSet<>();
        for (String word : words) {
            String normal = normalize(word);
            if (searchable(normal)) {
                lightStrings.add(new LightString(normal));
            }
        }
        return lightStrings;
    }

    public static String normalize(String s) {
        return s.toLowerCase(RU)
                .replace('ё', 'е')
                .replace("\u0301", "");
    }

    public static int hashCode(byte[] bytes) {
        int state = 0;
        for (int i = 0; i < bytes.length; i++) {
            state += bytes[i];
            for (int j = 0; j < 4; j++) {
                state *= 0x7C824F73;
                state ^= 0x5C12FE83;
                state = Integer.rotateLeft(state, 5);
            }
        }
        return state;
    }

    public static boolean searchable(String s) {
        return !s.isEmpty() && s.matches("[a-zа-я0-9-]+");
    }

    public static LightString[] toArray(Set<LightString> set) {
        LightString[] a = new LightString[set.size()];
        int i = 0;
        for (LightString val : set) a[i++] = val;
        return a;
    }

    public static int[] merge(int[] a, int[] b) {
        int[] answer = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        while (i < a.length && j < b.length)
            answer[k++] = a[i] < b[j] ? a[i++] :  b[j++];

        while (i < a.length)
            answer[k++] = a[i++];


        while (j < b.length)
            answer[k++] = b[j++];

        return answer;
    }
}
