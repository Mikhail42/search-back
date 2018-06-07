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
        return Util.hashCode(bytes);
    }

    /**
     * @param from the initial index of the range to be copied, inclusive
     * @param until the final index of the range to be copied, exclusive.
     *              (This index may lie outside the array.)
     */
    public LightString substring(int from, int until) {
        return new LightString(Arrays.copyOfRange(bytes, from, until));
    }

    public LightString substring(int from) {
        return new LightString(Arrays.copyOfRange(bytes, from, bytes.length));
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
            chars[i] = fromByte(bytes[i]);
        }
        return new String(chars);
    }

    public boolean startWith(char c) {
        return fromByte(bytes[0]) == c;
    }

    private static char fromByte(byte b) {
        return (char)((b >= ':' && b <= 90)  //90 == ':' + 32
            ? ('а' + (b - ':'))  // Russian А
            : b);
    }

    private static byte fromChar(char c) {
        return (byte) ((c >= 'а' && c <= 'я')
                ? (':' + (c - 'а'))
                : c);
    }

    private static byte[] ruEnDigits(String word) {
        char[] chars = word.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = fromChar(chars[i]);
        }
        return bytes;
    }

    @Override
    public String toString() {
        return asString();
    }
}
