package org.ionkin.search;

import java.util.Arrays;

public class ByteArray {
    private int pos = 0;

    private byte[] bytes;

    public ByteArray() {
        this.bytes = new byte[1];
    }

    public ByteArray(int size) {
        this.bytes = new byte[size];
    }

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public void add(LightString s) {
        add((byte) s.length());
        add(s.getBytes());
    }

    public void add(byte b) {
        if (pos == bytes.length) {
            resize();
        }
        bytes[pos++] = b;
    }

    public void addVb(int i) {
        add(VariableByte.compress(i));
    }

    //int[] out = new int[ar.length()];
    //IntWrapper outPos = new IntWrapper();
    //Simple9.compress(ar.getAll(), new IntWrapper(ar.getFrom()), ar.length(), out, outPos);
    public void addRange(IntsRange ar) {
        int diff = bytes.length - pos - ar.length() * 4;
        if (diff < 0) {
            resize((bytes.length + diff) * 2);
        }
        for (int i = 0; i < ar.length(); i++) {
            IO.putInt(bytes, ar.get(i), pos);
        }
    }

    public void add(byte[] b2) {
        add(new BytesRange(b2, 0, b2.length));
    }

    public void add(BytesRange range) {
        if (range.length() > bytes.length - pos) {
            resize((range.length() + bytes.length) * 2);
        }
        System.arraycopy(range.getAll(), range.getFrom(), bytes, pos, range.length());
        pos += range.length();
    }

    public void add(byte[]... mat) {
        int addLength = 0;
        for (int i = 0; i < mat.length; i++) {
            addLength += mat[i].length;
        }
        if (addLength <= bytes.length - pos) {
            for (int i = 0; i < mat.length; i++) {
                System.arraycopy(mat[i], 0, bytes, pos, mat[i].length);
                pos += mat[i].length;
            }
        } else {
            byte[] newAr = new byte[pos + addLength];
            System.arraycopy(bytes, 0, newAr, 0, pos);
            for (int i = 0; i < mat.length; i++) {
                System.arraycopy(mat[i], 0, bytes, pos, mat[i].length);
                pos += mat[i].length;
            }
            bytes = newAr;
        }
    }

    private void resize() {
        resize(pos * 2);
    }

    private void resize(int newSize) {
        byte[] news = new byte[newSize];
        System.arraycopy(bytes, 0, news, 0, bytes.length);
        bytes = news;
    }

    public byte[] getCopy() {
        return Arrays.copyOf(bytes, pos);
    }

    public byte[] getAll() {
        return bytes;
    }

    public byte get(int i) {
        return bytes[i];
    }

    public int size() {
        return pos;
    }
}
