package org.ionkin.search;

import com.google.common.base.Splitter;

import java.util.*;
import java.util.regex.Pattern;

public class Util {
    public static final String basePath = System.getProperty("user.home") + "/workspace/wiki-bz2/";
    public static final String textPath = basePath + "text/";
    public static final String indexFolder = basePath + "index/";
    public static final String positionIndexFolder = basePath + "posindex/";
    public static final String indexPath = basePath + "index.chmsb";
    public static final String testIndexPath = basePath + "testIndex.chmsb";
    public static final String positionsPath = basePath + "positions.chmsp";
    public static final String testPositionsPath = basePath + "testPositions.chmsp";
    public static final String tokensPath = basePath + "tokens.chsls";
    public static final String wordLemmPath = basePath + "lemm/allWordMap.chmss";
    public static final String docidPosPath = basePath + "docPositions.chmiiiFast";
    public static final String firstDocidFilenamePath = basePath + "firstDocidFilenameMap.csv";

    public static final String wordSymbol = "\\p{L}\\p{N}\u0301";
    public static final String ruEnLowerSymbol = "a-zа-я0-9";
    public static final Splitter splitPatternLazy = Splitter.onPattern("[^" + wordSymbol + "]+");
    public static final Pattern splitPattern = Pattern.compile("[^" + wordSymbol + "]+");
    public static final Pattern wordPattern = Pattern.compile("[" + wordSymbol + "]+");
    public static Pattern searchablePatter = Pattern.compile("[" + ruEnLowerSymbol + "]+");

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
        return !s.isEmpty() && searchablePatter.matcher(s).matches();
    }

    public static LightString[] toArray(Set<LightString> set) {
        LightString[] a = new LightString[set.size()];
        int i = 0;
        for (LightString val : set) a[i++] = val;
        return a;
    }

    public static int[] mergeSimple(int[][] mat) {
        int size = 0;
        for (int[] m : mat) size += m.length;
        IntArray ar = new IntArray(size);
        for (int[] m : mat) ar.add(m);
        int[] res = ar.getAll();
        Arrays.sort(res);
        return res;
    }

    public static int[] merge(Collection<int[]> mat) {
        int[][] mat2 = new int[mat.size()][];
        int i = 0;
        for (int[] ar : mat) {
            mat2[i++] = ar;
        }
        return merge(mat2);
    }

    public static int[] merge(int[][] mat) {
        int size = 0;
        int[] is = new int[mat.length];
        for (int[] m : mat) size += m.length;
        IntArray ar = new IntArray(size);
        int max = 0;
        for (int[] row : mat) {
            if (row.length > 0 && row[row.length - 1] > max) max = row[row.length - 1];
        }
        int min = 0;
        while (min < max) {
            // select next min0
            for (int k = 0; k < mat.length; k++) {
                if (is[k] < mat[k].length) {
                    min = mat[k][is[k]];
                    break;
                }
            }
            // find min with min0
            for (int k = 0; k < mat.length; k++) {
                if (is[k] < mat[k].length && min > mat[k][is[k]]) min = mat[k][is[k]];
            }
            // increment index for arrays with min value
            for (int k = 0; k < mat.length; k++) {
                if (is[k] < mat[k].length && min == mat[k][is[k]]) is[k]++;
            }
            ar.add(min);
        }
        return ar.getCopy();
    }

    public static int[] merge(int[] a, int[] b) {
        int[] answer = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) answer[k++] = a[i++];
            else if (a[i] > b[j]) answer[k++] = b[j++];
            else {
                answer[k++] = b[j++];
                i++;
            }
        }

        if (i < a.length) {
            if (a[i] != answer[k]) answer[k++] = a[i];
            i++;
        }
        while (i < a.length)
            answer[k++] = a[i++];

        if (j < b.length) {
            if (b[j] != answer[k]) answer[k++] = b[j];
            j++;
        }
        while (j < b.length)
            answer[k++] = b[j++];

        return k == answer.length ? answer : Arrays.copyOf(answer, k);
    }

    public static <K, V extends Comparable<? super V>> List<K> sortAscByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        List<K> result = new ArrayList<>(map.size() + 1);
        for (Map.Entry<K, V> entry : list) {
            result.add(entry.getKey());
        }

        return result;
    }
}
