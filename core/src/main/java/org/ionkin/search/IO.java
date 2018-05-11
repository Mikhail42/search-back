package org.ionkin.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IO {
    private static final Logger logger = LoggerFactory.getLogger(IO.class);

    public static void write(byte[] ar, String fileName) throws IOException {
        logger.debug("write to '{}'. size = {}", fileName, ar.length);
        try (FileChannel rwChannel = new RandomAccessFile(fileName, "rw").getChannel()) {
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, ar.length);
            wrBuf.put(ar);
        }
        logger.debug("write successfully");
    }

    public static byte[] read(String fileName, int skip, int length) throws IOException {
        logger.debug("read from '{}'. length = {}", fileName, length);
        try (FileChannel readChannel = new RandomAccessFile(fileName, "r").getChannel()) {
            ByteBuffer wrBuf = readChannel.map(FileChannel.MapMode.READ_ONLY, skip, length);
            byte[] fileContent = new byte[length];
            wrBuf = wrBuf.asReadOnlyBuffer().get(fileContent);
            logger.debug("read successfully");
            return fileContent;
        }
    }

    public static byte[] read(String fileName) throws IOException {
        try (FileChannel readChannel = new RandomAccessFile(fileName, "r").getChannel()) {
            logger.debug("read from '{}'. size = {}", fileName, readChannel.size());
            ByteBuffer wrBuf = readChannel.map(FileChannel.MapMode.READ_ONLY, 0, readChannel.size());
            byte[] fileContent = new byte[(int) readChannel.size()];
            wrBuf = wrBuf.asReadOnlyBuffer().get(fileContent);
            logger.debug("read successfully");
            return fileContent;
        }
    }

    public static void putInt(byte[] ar, int b, int pos) {
        ar[pos] = (byte) (b >>> 24);
        ar[pos + 1] = (byte) (b >>> 16);
        ar[pos + 2] = (byte) (b >>> 8);
        ar[pos + 3] = (byte) b;
    }

    public static int readInt(byte[] bytes, int offset) {
        int ret = 0;
        for (int i = 0; i < 4 && i < bytes.length; i++) {
            ret <<= 8;
            ret |= (int) bytes[i + offset] & 0xFF;
        }
        return ret;
    }

    public static void putString(byte[] ar, LightString b, int pos) {
        byte length = (byte) (b.length() & 0x7F);
        ar[pos] = length;
        System.arraycopy(b.getBytes(), 0, ar, pos + 1, b.length());
    }

    public static void putStringWithLength(byte[] ar, String b, int pos) {
        putInt(ar, b.length(), pos);
        // ommit 2 bytes
        System.arraycopy(b.getBytes(StandardCharsets.UTF_16), 2, ar, pos + 4, b.length() * 2);
    }

    public static String readString(byte[] ar, int pos, int length) {
        byte[] str = new byte[length * 2 + 2];
        str[0] = -2;
        str[1] = -1;
        System.arraycopy(ar, pos, str, 2, str.length - 2);
        return new String(str, StandardCharsets.UTF_16);
    }

    public static LightString readString(byte[] bs, int startPos) {
        byte strLength = bs[startPos];
        byte[] str = new byte[strLength];
        System.arraycopy(bs, startPos + 1, str, 0, strLength);
        return new LightString(str);
    }

    public static void putString(ByteBuffer wrBuf, LightString str) {
        byte[] bytes = str.getBytes();
        byte length = (byte) (bytes.length & 0x7F);
        wrBuf.put(length);
        wrBuf.put(bytes, 0, length);
    }

    public static void putArrayBytesWithLength(ByteBuffer wrBuf, byte[] ar) {
        wrBuf.putInt(ar.length);
        wrBuf.put(ar);
    }

    public static void putArrayBytesWithLength(byte[] res, byte[] part, int pos) {
        ArrayList<Byte> lengthComp = VariableByte.compress(part.length);
        for (int i = 0; i < lengthComp.size(); i++) {
            res[pos + i] = lengthComp.get(i);
        }
        System.arraycopy(part, 0, res, pos + lengthComp.size(), part.length);
    }

    public static void putArrayIntWithLength(ByteBuffer wrBuf, int[] ar) {
        wrBuf.putInt(ar.length);
        for (int ind : ar) {
            wrBuf.putInt(ind);
        }
    }

    public static int[] readArrayIntWithLength(byte[] bs, int startPos) {
        int idsLength = readInt(bs, startPos);
        byte[] idsAsBytes = new byte[idsLength * 4];
        System.arraycopy(bs, startPos + 4, idsAsBytes, 0, idsAsBytes.length);
        int[] ids = new int[idsLength];
        for (int i = 0; i < idsLength; i++) {
            ids[i] = readInt(bs, startPos + (i + 1) * 4);
        }
        return ids;
    }

    public static int[] readArrayInt(byte[] bs, int startPos, int intsLength) {
        int[] ids = new int[intsLength];
        for (int i = 0; i < intsLength; i++) {
            ids[i] = readInt(bs, startPos + i * 4);
        }
        return ids;
    }

    public static int[] toInts(byte[] bytes) {
        int[] res = new int[bytes.length / 4];
        for (int i = 0; i < res.length; i++) {
            res[i] = readInt(bytes, i * 4);
        }
        return res;
    }

    public static byte[] toBytes(int[] ar) {
        ByteBuffer buf = ByteBuffer.allocate(ar.length * 4);
        IntBuffer intBuffer = buf.asIntBuffer();
        intBuffer.put(ar);
        return buf.array();
    }

    /*public static byte[] put(byte[] ar, int[] toAdd) {
        byte[] small = toBytes(toAdd);
        byte[] res = new byte[ar.length + small.length];
        System.arraycopy(ar, 0, res, 0, ar.length);
        System.arraycopy(small, 0, res, ar.length, small.length);
        return res;
    }*/
}
