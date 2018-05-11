package org.ionkin.search;

import org.ionkin.search.set.CompactHashSet;

public class SpellChecker {

    private final CompactHashSet<LightString> allStrings;

    public SpellChecker(CompactHashSet<LightString> allStrings) {
        this.allStrings = allStrings;
    }
/*
    public LightString toCorrectWord(LightString word) {
        if (isCorrectWord(word)) {
            return word;
        } else {
            if ()
        }
    }
*/
    public boolean isCorrectWord(LightString word) {
        return allStrings.contains(word);
    }
}
