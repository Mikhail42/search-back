/**
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 * <p>
 * (c) Daniel Lemire, http://lemire.me/en/
 */
package org.ionkin.search;


import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of variable-byte
 * <p>
 * Note that this does not use differential coding: if you are working on sorted
 * lists, you must compute the deltas separately.
 *
 * @author Daniel Lemire
 * @editor Mikhail Ionkin
 */
public class VariableByte {

    public static int compressedLengthOfLength(byte[] ar) {
        int n = ar.length;
        return compressedLength(n);
    }

    public static int compressedLength(int n) {
        return n < 0x80 ? 1 : n < 0x4000 ? 2 : n < 0x200000 ? 3 : n < 0x10000000 ? 4 : 5;
    }

    public static byte[] compress(int[] in) {
        int size = 0;
        for (int i : in) {
            size += compressedLength(i);
        }
        ByteArray res = new ByteArray(size);
        for (int k = 0; k < in.length; ++k) {
            addCompressed(res, in[k]);
        }
        return res.getAll();
    }

    public static byte[] compress(int val) {
        ByteArray res = new ByteArray(compressedLength(val));
        addCompressed(res, val);
        return res.getAll();
    }

    public static void addCompressed(ByteArray acc, int val) {
        if (val < 0 || val >= 0x10000000) {
            // if error is occur, use long
            throw new RuntimeException("Can't compress " + val + ", because it is less than 0 or more or equals than 2^28");
        } else if (val < 0x80) {
            acc.add((byte) (val | 0x80));
        } else if (val < 0x4000) {
            acc.add((byte) (val & 0x7F));
            acc.add((byte) ((val >> 7) | 0x80));
        } else if (val < 0x200000) {
            acc.add((byte) (val & 0x7F));
            acc.add((byte) ((val >> 7) & 0x7F));
            acc.add((byte) ((val >> 14) | 0x80));
            // if (val < 0x10000000)
        } else {
            acc.add((byte) (val & 0x7F));
            acc.add((byte) ((val >> 7) & 0x7F));
            acc.add((byte) ((val >> 14) & 0x7F));
            acc.add((byte) ((val >> 21) | 0x80));
        }
    }

    public static int decompressSize(BytesRange range) {
        return decompressSize(range.getAll(), range.getFrom(), range.getTo());
    }

    public static int decompressSize(byte[] in, int from, int until) {
        int count = 0;
        for (int i = from; i < until; i++) {
            if (in[i] < 0) count++;
        }
        return count;
    }

    public static int[] uncompress(byte[] in, IntWrapper from, int take) {
        return uncompress(in, from, in.length, take);
    }

    public static int[] uncompress(byte[] in, IntWrapper from, int until, int take) {
        List<Integer> res = new ArrayList<>();
        int v;
        int count = 0;
        int p = from.get();
        for (; p < until && count < take; count++, res.add(v)) {
            v = in[p] & 0x7F;
            if (in[p] < 0) {
                p += 1;
                continue;
            }
            v = ((in[p + 1] & 0x7F) << 7) | v;
            if (in[p + 1] < 0) {
                p += 2;
                continue;
            }
            v = ((in[p + 2] & 0x7F) << 14) | v;
            if (in[p + 2] < 0) {
                p += 3;
                continue;
            }
            v = ((in[p + 3] & 0x7F) << 21) | v;
            if (in[p + 3] < 0) {
                p += 4;
                continue;
            }
            v = ((in[p + 4] & 0x7F) << 28) | v;
            p += 5;
        }
        from.set(p);
        return Ints.toArray(res);
    }

    public static int getNextPos(byte[] in, int p) {
        int pos = p;
        while (in[pos++] >= 0 && pos < in.length);
        return pos;
    }

    public static int uncompressFirst(BytesRange range) {
        return uncompressFirst(range.getAll(), range.getFrom());
    }

    public static int uncompressFirst(BytesRange range, int from) {
        return uncompressFirst(range.getAll(), range.getFrom() + from);
    }

    public static int uncompressFirst(byte[] in, int p) {
        int v = in[p] & 0x7F;
        if (in[p] >= 0) {
            v = ((in[p + 1] & 0x7F) << 7) | v;
            if (in[p + 1] >= 0) {
                v = ((in[p + 2] & 0x7F) << 14) | v;
                if (in[p + 2] >= 0) {
                    v = ((in[p + 3] & 0x7F) << 21) | v;
                    if (in[p + 3] >= 0) {
                        v = ((in[p + 4] & 0x7F) << 28) | v;
                    }
                }
            }
        }
        return v;
    }

    public static int uncompressFirst(ByteBuffer readBuffer) {
        int a = readBuffer.get();
        int v = a & 0x7F;
        if (a >= 0) {
            a = readBuffer.get();
            v = ((a & 0x7F) << 7) | v;
            if (a >= 0) {
                a = readBuffer.get();
                v = ((a & 0x7F) << 14) | v;
                if (a >= 0) {
                    a = readBuffer.get();
                    v = ((a & 0x7F) << 21) | v;
                    if (a >= 0) {
                        a = readBuffer.get();
                        v = ((a & 0x7F) << 28) | v;
                    }
                }
            }
        }
        return v;
    }
}
