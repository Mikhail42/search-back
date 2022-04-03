package org.ionkin.search;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TokensStoreTest {

    @Test
    public void tokensTest() throws IOException {
        LightString[] tokens = TokensStore.getTokens();
        Assert.assertTrue(tokens.length > 100_000);
        Assert.assertTrue(tokens.length < 10_000_000);
        Assert.assertTrue(Arrays.stream(tokens).anyMatch(t -> t.asString().equals("слон")));
    }
}