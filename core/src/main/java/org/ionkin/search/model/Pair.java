package org.ionkin.search.model;

public class Pair<F, S> {
    public F key;
    public S value;

    public Pair(F first, S second) {
        this.key = first;
        this.value = second;
    }
}
