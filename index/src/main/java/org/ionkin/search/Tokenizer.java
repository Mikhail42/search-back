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

    /*@Deprecated
    public static void tokensOld() throws Exception {
        CompactHashSet<LightString> tokens = new CompactHashSet<>(new StringTranslator());
        Pattern pattern = Pattern.compile("[^/p{L}/p{N}\u0301-]+");
        PipelineBuilder.input(new SinglestreamXmlDumpParser(
                new File("/media/mikhail/Windows/Users/Misha/Downloads/ruwiki-20180201-pages-articles-multistream.xml" +
                        "/ruwiki-20180201-pages-articles-multistream.xml"), 500)).pipe(batch -> {
            if (!batch.isEmpty()) {
                for (Page p : batch) {
                    String[] strs = pattern.split(p.getContent());
                    for (String s : strs) {
                        String normal = Util.normalize(s);
                        if (Util.searchable(normal)) {
                            tokens.add(new LightString(normal));
                        }
                    }
                }
                logger.info("current id: {}", batch.get(0).getId());
            }
        }).run();
        tokens.write(basePath + "tokens/allTokens");
        logger.info("size: {}", tokens.size());
        logger.info("all");
    }*/

    public static void tokens() throws Exception {
        logger.info("start get tokens");
        CompactHashSet<LightString> tokens = new CompactHashSet<>(new StringTranslator());

        Pattern pattern = Pattern.compile("[^\\p{L}\\p{N}\u0301-]+");
        Iterator<Page> iterator = new TextArticleIterator().articleTextIterator();

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

        tokens.write("allTokens.chsls");
        logger.info("size: {}", tokens.size());
        logger.info("all");
    }
}
