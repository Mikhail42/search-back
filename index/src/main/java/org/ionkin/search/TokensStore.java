package org.ionkin.search;

import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TokensStore {
    private static final Logger logger = LoggerFactory.getLogger(TokensStore.class);

    private final LightString[] tokens;

    private TokensStore() throws IOException {
        logger.debug("try read tokens");
        CompactHashSet<LightString> tokensMap = CompactHashSet.read(Util.tokensPath, new StringTranslator());
        tokens = Util.toArray(tokensMap);
        tokensMap = null;
    }

    private static TokensStore instance;

    public static LightString[] getTokens() throws IOException {
        if (instance == null) {
            instance = new TokensStore();
        }
        return instance.tokens;
    }
}
