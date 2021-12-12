package org.ionkin.search.model;

public class IntIntPair {
    private final int first;
    private final int second;

    public IntIntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int first() {
        return first;
    }

    public int second() {
        return second;
    }
}
