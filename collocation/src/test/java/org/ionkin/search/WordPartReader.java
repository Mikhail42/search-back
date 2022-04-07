package org.ionkin.search;

import com.google.common.base.Splitter;
import org.ionkin.search.map.StringStringMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordPartReader {

    public static void main(String... args) throws IOException {
        StringStringMap ssm = new StringStringMap();

        String filename = Util.basePath + "lemm/lemms0530";
        String content = new String(IO.read(filename), StandardCharsets.UTF_8);

        Pattern pattern = Pattern.compile("^\\d+\\s+[\\p{L}p{N}-]+\\s+([\\p{L}p{N}-]+)\\s+([\\p{L}p{N}-]+)\\s+.*");
        final Splitter splitPatternLazy = Splitter.onPattern("\n");
        splitPatternLazy.split(content).forEach(str -> {
            Matcher m = pattern.matcher(str);
            if (m.find()) {
                ssm.putIfAbsent(new LightString(m.group(1)), new LightString(m.group(2).toLowerCase()));
            }
        });

        ssm.write(Util.basePath + "language-part.ssm");
    }
}
