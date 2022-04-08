package org.ionkin.search;

import org.junit.Test;
import org.scijava.parse.SyntaxTree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class EvaluatorPerformanceTest {

    @Test
    public void queryWords() {
        String query = "\"что  где  когда\"  &&  !\"хрустальная  сова\"";
        SyntaxTree tree = SyntaxTreeHelper.create(Normalizer.normalize(query));
        List<LightString> words = SyntaxTreeHelper.queryWords(tree);
        LightString[] answers =
                Stream.of("что  где  когда".split("\\s+")).map(LightString::new).toArray(LightString[]::new);
        assertArrayEquals(words.toArray(), answers);
    }

    @Test
    public void queryWords2() {
        String query = "что где  && когда  || почему зачем";
        SyntaxTree tree = SyntaxTreeHelper.create(Normalizer.normalize(query));
        List<LightString> words = SyntaxTreeHelper.queryWords(tree);
        Object[] ans = Stream.of("что  где  когда почему зачем".split("\\s+"))
                .map(LightString::new).toArray();
        assertArrayEquals(words.toArray(), ans);
    }

    @Test
    public void queryWords3() {
        String query = "\" что где  когда \"  &&  \" хрустальная  сова\"";
        SyntaxTree tree = SyntaxTreeHelper.create(Normalizer.normalize(query));
        List<LightString> words = SyntaxTreeHelper.queryWords(tree);
        Object[] ans = Stream.of("что  где  когда хрустальная  сова".split("\\s+"))
                .map(LightString::new).toArray();
        assertArrayEquals(words.toArray(), ans);
    }
}