package org.ionkin.search;

import org.ionkin.search.set.CompactHashSet;
import org.ionkin.search.set.StringTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.regex.Pattern;

public class Tokenizer {
    private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

    public static void main(String... args) throws Exception {
        tokens();
    }

    public static void tokens() throws Exception {
        logger.info("start get tokens");
        CompactHashSet<LightString> tokens = new CompactHashSet<>(new StringTranslator());

        Pattern pattern = Util.splitPattern;
        Iterator<Page> iterator = TextArticleIterator.articleTextIterator();

        while (iterator.hasNext()) {
            Page page = iterator.next();
            String[] strs = pattern.split(page.getContent());
            for (String s : strs) {
                String normal = Util.normalize(s);
                if (Util.searchable(normal)) {
                    tokens.add(new LightString(normal));
                }
            }
        }

        tokens.write(Util.dictionaryPath);
        logger.info("size: {}", tokens.size());
        logger.info("all");
    }
}
