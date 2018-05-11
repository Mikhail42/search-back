package org.ionkin.search;

import com.google.common.base.Splitter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {
    static final String basePath = "/media/mikhail/Windows/Users/Misha/workspace/wiki-bz2/";

    static final String xmlFileName = "/media/mikhail/Windows/Users/Misha/Downloads" +
            "/ruwiki-20180201-pages-articles-multistream.xml" +
            "/ruwiki-20180201-pages-articles-multistream.xml";

    public static final Splitter splitPattern = Splitter.onPattern("[^\\p{L}\\p{N}\u0301-]+");
    public static final Pattern splitJavaPattern = Pattern.compile("[^\\p{L}\\p{N}\u0301-]+");

    public static final Locale RU = new Locale("RU");

    public static Set<LightString> lightStrings(String pageContent) {
        Iterable<String> words = splitPattern.split(pageContent);
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
