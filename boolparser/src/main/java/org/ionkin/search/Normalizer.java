package org.ionkin.search;

public class Normalizer {
    public static String normalize(String s) {
        return Util.normalize(s)
                .replaceAll("[^A-Za-zА-Яа-я0-9 &|!()\\[\\]{}/«»\"]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
