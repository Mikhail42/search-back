package org.ionkin.search;

import java.util.Arrays;

public class IntsRange {
    private final int[] ar;
    private final int from;
    private final int to;

    public IntsRange(int[] ar) {
        this(ar, 0, ar.length);
    }

    public IntsRange(int[] ar, int from) {
        this(ar, from, ar.length);
    }

    public IntsRange(int[] ar, int from, int to) {
        this.ar = ar;
        this.from = from;
        this.to = to;
    }

    public byte[] toBytes() {
        return IO.toBytes(this);
    }

    public int get(int i) {
        return ar[i + from];
    }

    public int length() {
        return to - from;
    }

    public int[] getCopy() {
        return Arrays.copyOfRange(ar, from, to);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int[] getAll() {
        return ar;
    }
}
