package org.ionkin.search;

import java.util.Arrays;
import java.util.Objects;

public class BytesRange {
    private final byte[] ar;
    private final int from;
    private final int to;

    public BytesRange(byte[] ar) {
        this(ar, 0, ar.length);
    }

    public BytesRange(byte[] ar, int from) {
        this(ar, from, ar.length);
    }

    public BytesRange(byte[] ar, int from, int to) {
        this.ar = ar;
        this.from = from;
        this.to = to;
    }

    public byte get(int i) {
        return ar[i + from];
    }

    public int length() {
        return to - from;
    }

    public byte[] getCopy() {
            return Arrays.copyOfRange(ar, from, to);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public byte[] getAll() {
        return ar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BytesRange that = (BytesRange) o;
        return Arrays.equals(this.getCopy(), that.getCopy());
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(from, to);
        result = 31 * result + Arrays.hashCode(ar);
        return result;
    }
}
