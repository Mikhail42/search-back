package org.ionkin.search;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SerializerTest {

    @Test
    public void testSerializeDeserialize() throws IOException {
        File tempFile = File.createTempFile("MyAppName-", ".tmp");
        tempFile.deleteOnExit();
        String filename = tempFile.getAbsolutePath();

        Map<LightString, Integer> map = new HashMap<>();
        map.put(new LightString("word"), 100);
        map.put(new LightString("all"), 200);
        Serializer.serialize(map, filename);
        Map<LightString, Integer> map2 = (Map) Serializer.deserialize(filename);
        assertEquals(map2.get(new LightString("word")), new Integer(100));
        assertEquals(map2.get(new LightString("all")), new Integer(200));
    }

    @Test
    public void testSerializeDeserializeArrayOfString() throws IOException {
        File tempFile = File.createTempFile("MyAppName-", ".tmp");
        tempFile.deleteOnExit();
        String filename = tempFile.getAbsolutePath();

        LightString[] ar = new LightString[] {
                new LightString("мама"),
                new LightString("папа-2")
        };
        Serializer.serializeArrayOfString(ar, filename);
        LightString[] ar2 = Serializer.deserializeArrayOfString(filename, 2);
        assertTrue(Arrays.equals(ar, ar2));
    }

    @Test
    public void testSerializeDeserializeListOfString() throws IOException {
        File tempFile = File.createTempFile("MyAppName-", ".tmp");
        tempFile.deleteOnExit();
        String filename = tempFile.getAbsolutePath();

        List<LightString> ar = new LinkedList<>();
        ar.add(new LightString("мама"));
        ar.add(new LightString("папа-2"));

        Serializer.serializeListOfString(ar, filename);
        List<LightString> ar2 = Serializer.deserializeListOfString(filename);
        assertTrue(CollectionUtils.isEqualCollection(ar, ar2));
    }
}