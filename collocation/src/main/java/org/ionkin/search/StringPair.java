package org.ionkin.search;

import java.util.Objects;

public class StringPair {

    private final LightString s1;
    private final LightString s2;

    public StringPair(LightString s1, LightString s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public StringPair(String s1, String s2) {
        this(new LightString(s1), new LightString(s2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringPair that = (StringPair) o;
        return Objects.equals(s1, that.s1) &&
                Objects.equals(s2, that.s2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(s1, s2);
    }

    public LightString getS1() {
        return s1;
    }

    public LightString getS2() {
        return s2;
    }
}
