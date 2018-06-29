package org.ionkin.search;

import java.util.Objects;

public class IntPair {

    private final int i1;
    private final int i2;

    public IntPair(int i1, int i2) {
        this.i1 = i1;
        this.i2 = i2;
    }

    public int getI1() {
        return i1;
    }

    public int getI2() {
        return i2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntPair intPair = (IntPair) o;
        return i1 == intPair.i1 &&
                i2 == intPair.i2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i1, i2);
    }
}
