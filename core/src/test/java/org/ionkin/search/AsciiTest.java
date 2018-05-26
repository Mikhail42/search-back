package org.ionkin.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AsciiTest {

    @Test
    public void asString() {
        assertEquals(new LightString("мама").asString(), "мама");
    }

    @Test
    public void toStringTest() {
        assertEquals(new LightString("мама").toString(), "мама");
    }

    @Test
    public void length() {
        assertEquals(new LightString("мама").length(), 4);
    }

    @Test
    public void fromBytes() {
        assertEquals(new LightString(new LightString("мама").getBytes()), new LightString("мама"));
    }

}