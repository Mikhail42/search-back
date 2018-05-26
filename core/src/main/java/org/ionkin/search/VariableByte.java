/**
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 * <p>
 * (c) Daniel Lemire, http://lemire.me/en/
 */
package org.ionkin.search;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.omg.PortableInterceptor.INACTIVE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @editor M. Ionkin
 */
public class VariableByte {
    private static final Logger logger = LoggerFactory.getLogger(VariableByte.class);

    private static byte extract7bits(int i, int val) {
        // ((val >> (7 * i)) & (0x80 - 1));
        return (byte) ((val >> ((i << 3) - i)) & 0x7F);
    }

    private static byte extract7bitsmaskless(int i, int val) {
        // ((val >> (7 * i)));
        return (byte) ((val >> ((i << 3) - i)));
    }

    public static int compressedLengthOfLength(byte[] ar) {
        int n = ar.length;
        return compressedLength(n);
    }

    public static int compressedLength(int n) {
        return n < 0x80
                ? 1
                : n < 0x4000
                ? 2
                : n < 0x200000
                ? 3
                : n < 0x10000000
                ? 4
                : -1;
    }

    public static byte[] compress(int[] in) {
        List<Byte> res = new ArrayList<>();
        for (int k = 0; k < in.length; ++k) {
            addCompressed(res, in[k]);
        }
        return Bytes.toArray(res);
    }

    public static ArrayList<Byte> compress(int val) {
        ArrayList<Byte> res = new ArrayList<>(1);
        addCompressed(res, val);
        return res;
    }

    public static byte[] compressToBytes(int val) {
        ArrayList<Byte> res = new ArrayList<>(1);
        addCompressed(res, val);
        return Bytes.toArray(res);
    }

    public static void addCompressed(List<Byte> acc, int val) {
        if (val < 0 || val >= 0x10000000) {
            // if error is occur, use long
            throw new RuntimeException("Can't compress " + val + ", because it is less than 0 or more or equals than 2^28");
        } else if (val < 0x80) {
            acc.add((byte) (val | 0x80));
        } else if (val < 0x4000) {
            acc.add(extract7bits(0, val));
            acc.add((byte) (extract7bitsmaskless(1, val) | 0x80));
        } else if (val < 0x200000) {
            acc.add(extract7bits(0, val));
            acc.add(extract7bits(1, val));
            acc.add((byte) (extract7bitsmaskless(2, val) | 0x80));
            // if (val < 0x10000000)
        } else {
            acc.add(extract7bits(0, val));
            acc.add(extract7bits(1, val));
            acc.add(extract7bits(2, val));
            acc.add((byte) (extract7bitsmaskless(3, val) | 0x80));
        }
    }

    public static int[] uncompress(byte[] in, int take) {
        return uncompress(in, 0, take);
    }

    public static int[] uncompress(byte[] in, int p, int take) {
        List<Integer> res = new ArrayList<>();
        int v;
        int count = 0;
        for (; p < in.length && count < take; count++, res.add(v)) {
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

        return Ints.toArray(res);
    }

    public static int getNextPos(byte[] in, int p) {
        int pos = p;
        while (in[pos++] > 0 && pos < in.length);
        return pos;
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
