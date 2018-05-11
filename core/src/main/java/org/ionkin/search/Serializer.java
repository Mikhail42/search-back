package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringBytesTranslator;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTSerializerRegistry;
import org.nustaq.serialization.serializers.FSTClassSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ionkin.search.IO.*;

public class Serializer {
    private static final Logger logger = LoggerFactory.getLogger(IO.class);

    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    {
        FSTSerializerRegistry reg = conf.getCLInfoRegistry().getSerializerRegistry();
        reg.putSerializer(CompactHashMap.class, new FSTClassSerializer(), false);
        reg.putSerializer(byte[].class, new FSTClassSerializer(), false);
        reg.putSerializer(byte[][].class, new FSTClassSerializer(), false);
        reg.putSerializer(StringBytesTranslator.class, new FSTClassSerializer(), false);
    }
    public static void serialize(Object o, String fileName) throws IOException {
        logger.debug("start serialize");
        byte[] barray = conf.asByteArray(o);
        logger.debug("bytes are created. size={}", barray.length);
        write(barray, fileName);
    }

    public static Object deserialize(String fileName) throws IOException {
        byte[] fileContent = read(fileName);
        return conf.asObject(fileContent);
    }

    public static LightString[] deserializeArrayOfString(String fileName, int size) throws IOException {
        LightString[] result = new LightString[size];
        byte[] bytes = read(fileName);
        int pos = 0;
        while (pos < bytes.length) {
            int id = readInt(bytes, pos); pos += 4;
            byte length = bytes[pos]; pos++;
            byte[] ar = Arrays.copyOfRange(bytes, pos, pos + length); pos += length;
            result[id] = new LightString(ar);
        }
        return result;
    }

    public static void serializeArrayOfString(LightString[] ar, String fileName) throws IOException {
        try (FileChannel rwChannel = new RandomAccessFile(fileName, "rw").getChannel()) {
            int size = 0;
            for (LightString ls : ar) {
                if (ls != null) {
                    size += (ls.length() & 0x7F) + 1 + 4;
                }
            }
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            for (int i=0; i<ar.length; i++) {
                if (ar[i] != null) {
                    wrBuf.putInt(i);
                    putString(wrBuf, ar[i]);
                }
            }
        }
    }

    public static void serializeListOfString(List<LightString> list, String fileName) throws IOException {
        try (FileChannel rwChannel = new RandomAccessFile(fileName, "rw").getChannel()) {
            int size = 0;
            for (LightString ls : list) {
                size += (ls.length() & 0x7F) + 1;
            }
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            for (LightString ls : list) {
                putString(wrBuf, ls);
            }
        }
    }

    public static List<LightString> deserializeListOfString(String fileName) throws IOException {
        byte[] fromFile = read(fileName);
        List<LightString> list = new ArrayList<>();
        int pos = 0;
        while (pos < fromFile.length) {
            int length = fromFile[pos]; pos++;
            byte[] str = Arrays.copyOfRange(fromFile, pos, pos + length);
            pos += length;
            list.add(new LightString(str));
        }
        return list;
    }
}
