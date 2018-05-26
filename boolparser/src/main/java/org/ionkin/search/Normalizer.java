package org.ionkin.search;

import java.util.Locale;

public class Normalizer {

    private static final Locale RU = new Locale("RU");
    public static String normalize(String query) {
        return query.toLowerCase(RU).trim()
                .replace('ё', 'е')
                .replaceAll("\u0301", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\[", "(").replaceAll("]", ")")
                .replaceAll("[^A-Za-zА-Яа-я0-9 &|!()/\"]+", "")
                ;
    }
}
