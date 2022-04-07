package org.ionkin.search;

import java.util.Arrays;

public class ByteArray {
    private int pos = 0;

    private byte[] bytes;

    public ByteArray() {
        this(1);
    }

    public ByteArray(int size) {
        this.bytes = new byte[size];
    }

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
        this.pos = bytes.length;
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

    public int size() {
        return pos;
    }
}
