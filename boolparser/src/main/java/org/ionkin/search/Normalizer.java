package org.ionkin.search;

public class Normalizer {
    public static String normalize(String s) {
        return Util.normalize(s)
                .replaceAll("[^a-zа-я0-9 &|!()\\[\\]{}/\"]+", " ")
                .replaceAll("\\s+", " ")
                .replaceAll(" ([])}\"])", "$1")
                .replaceAll("([\\[({\"]) ", "$1")
                .trim();
    }
}
