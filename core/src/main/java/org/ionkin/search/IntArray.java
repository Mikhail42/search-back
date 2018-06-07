package org.ionkin.search;

import java.util.Arrays;

public class IntArray {
    private int pos = 0;

    private int[] ints;

    public IntArray() {
        this.ints = new int[1];
    }

    public IntArray(int size) {
        this.ints = new int[size];
    }

    public void add(int b) {
        if (pos == ints.length) {
            resize();
        }
        ints[pos++] = b;
    }

    public void add(int[] b2) {
        if (b2.length <= ints.length - pos) {
            System.arraycopy(b2, 0, ints, pos, b2.length);
        } else {
            int[] newAr = new int[pos + b2.length];
            System.arraycopy(ints, 0, newAr, 0, pos);
            System.arraycopy(b2, 0, newAr, pos, b2.length);
            ints = newAr;
        }
        pos += b2.length;
    }

    public void add(int[]... mat) {
        int addLength = 0;
        for (int i = 0; i < mat.length; i++) {
            addLength += mat[i].length;
        }
        if (addLength <= ints.length - pos) {
            for (int i = 0; i < mat.length; i++) {
                System.arraycopy(mat[i], 0, ints, pos, mat[i].length);
                pos += mat[i].length;
            }
        } else {
            int[] newAr = new int[pos + addLength];
            System.arraycopy(ints, 0, newAr, 0, pos);
            for (int i = 0; i < mat.length; i++) {
                System.arraycopy(mat[i], 0, ints, pos, mat[i].length);
                pos += mat[i].length;
            }
            ints = newAr;
        }
    }

    private void resize() {
        int[] news = new int[pos * 2];
        System.arraycopy(ints, 0, news, 0, ints.length);
        ints = news;
    }

    public int[] getCopy() {
        return Arrays.copyOf(ints, pos);
    }

    public int[] getAll() {
        return ints;
    }

    public int get(int i) {
        return ints[i];
    }
    
    public int size() {
        return pos;
    }
}
