package org.ionkin.search;

import org.ionkin.search.map.CompactHashMap;
import org.ionkin.search.map.StringIntTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.lth.cs.nlp.mediawiki.model.Page;
import se.lth.cs.nlp.mediawiki.parser.SinglestreamXmlDumpParser;
import se.lth.cs.nlp.pipeline.PipelineBuilder;

import java.io.File;

import static org.ionkin.search.Util.basePath;

public class Analizer {
    private static final Logger logger = LoggerFactory.getLogger(Analizer.class);

    public static void frequence() throws Exception {
        CompactHashMap<LightString, Integer> tokens = new CompactHashMap<>(new StringIntTranslator());
        PipelineBuilder.input(new SinglestreamXmlDumpParser(
                new File("/media/mikhail/Windows/Users/Misha/Downloads/ruwiki-20180201-pages-articles-multistream.xml" +
                        "/ruwiki-20180201-pages-articles-multistream.xml"), 500)).pipe(batch -> {
            if (!batch.isEmpty()) {
                for (Page p : batch) {
                    Iterable<String> strs = Util.splitPattern.split(p.getContent());
                    for (String s : strs) {
                        String normal = Util.normalize(s);
                        if (Util.searchable(normal)) {
                            LightString ls = new LightString(normal);
                            int count = tokens.getOrDefault(ls, 0);
                            tokens.put(ls, count + 1);
                        }
                    }
                }
                logger.info("current id: {}", batch.get(0).getId());
            }
        }).run();
        tokens.write(basePath + "freq/allTokens");
        logger.info("size: {}", tokens.size());
        logger.info("all");
    }
}
