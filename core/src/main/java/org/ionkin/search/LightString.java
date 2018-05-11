package org.ionkin.search;

import java.io.Serializable;
import java.util.Arrays;

public class LightString implements Serializable {

    private static final long serialVersionUID = 1989468575405622023L;

    private final byte[] bytes;

    public LightString(String s) {
        this(ruEnDigits(s));
    }

    public LightString(byte[] bytes) {
        this.bytes = (bytes.length <= 0x7F) ? bytes : Arrays.copyOfRange(bytes, 0, 0x7F);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightString that = (LightString) o;
        return hashCode() == that.hashCode() &&
                Arrays.equals(bytes, that.bytes);
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public int hashCode() {
        int state = 0;
        for (int i = 0; i < bytes.length; i++) {
            state += bytes[i];
            for (int j = 0; j < 4; j++) {
                state *= 0x7C824F73;
                state ^= 0x5C12FE83;
                state = Integer.rotateLeft(state, 5);
            }
        }
        return state;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String asString() {
        return ruEnDigits(bytes);
    }

    private static String ruEnDigits(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b >= ':' && b <= 90) { //90 == ':' + 32
                // Russian А
                chars[i] = (char) ('а' + (b - ':'));
            } else {
                chars[i] = (char) b;
            }
        }
        return new String(chars);
    }

    private static byte[] ruEnDigits(String word) {
        char[] chars = word.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < bytes.length; i++) {
            char c = chars[i];
            if (c >= 'а' && c <= 'я') {
                bytes[i] = (byte) (':' + (c - 'а'));
            } else {
                bytes[i] = (byte) c;
            }
        }
        return bytes;
    }

    @Override
    public String toString() {
        return asString();
    }
}
