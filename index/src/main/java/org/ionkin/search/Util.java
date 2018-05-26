package org.ionkin.search;

import com.google.common.base.Splitter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {
    public static final String basePath = "C:/Users/Misha/workspace/wiki-bz2/";
    public static final String indexFolder = basePath + "index/";
    public static final String positionIndexFolder = basePath + "posindex/";
    public static final String indexPath = basePath + "allIndex.lsbytesFast";
    public static final String textPath = basePath + "text/";
    public static final String positionsPath = basePath + "joinAll";

    public static final String wordSymbol = "\\p{L}\u0301-";
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
        s = s.toLowerCase(RU);
        s = s.replace('ё', 'е');
        s = s.replace("\u0301", "");
        return s;
    }

    public static boolean searchable(String s) {
        return !s.isEmpty() && s.matches("[a-zа-я0-9-]+");
    }
}
