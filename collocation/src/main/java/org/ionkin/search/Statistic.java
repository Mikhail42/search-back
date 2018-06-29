package org.ionkin.search;

import org.ionkin.search.map.IntStringMap;
import org.ionkin.search.map.StringStringMap;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Statistic {
    static int sum(int[] ar, int from, int until) {
        int s = 0;
        for (int i = from; i < until; i++) {
            s += ar[i];
        }
        return s;
    }

    static double mean(int[] ar, int from, int until) {
        int s = sum(ar, from, until);
        return ((double) s) / (until - from);
    }

    static int sumByValue(IntPair[] ar, int from, int until) {
        int s = 0;
        for (int i = from; i < until; i++) {
            s += ar[i].getI2();
        }
        return s;
    }

    static double meanByValue(IntPair[] ar, int from, int until) {
        int s = sumByValue(ar, from, until);
        return ((double) s) / (until - from);
    }

    static double disp(int[] ar, int from, int until) {
        double E = mean(ar, from, until);
        return disp(ar, E, from, until);
    }

    static double disp(int[] ar, double E, int from, int until) {
       // double E = mean(ar, from, until);
        double s = 0;
        for (int i = from; i < until; i++) {
            s += sqr(ar[i] - E);
        }
        return s / (until - from - 1);
    }

    static double dispByValue(IntPair[] ar, int from, int until) {
        double E = meanByValue(ar, from, until);
        return dispByValue(ar, E, from, until);
    }

    static double dispByValue(IntPair[] ar, double E, int from, int until) {
        double s = 0;
        for (int i = from; i < until; i++) {
            s += sqr(ar[i].getI2() - E);
        }
        return s / (until - from - 1);
    }

    static double sqr(double x) {
        return x*x;
    }

    static final Set<LightString> exc = Stream.of("по", "еще", "за", "их", "г", "до", "ий", "ее", "же", "век", "оно", "ный", "это", "ль")
            .map(LightString::new).collect(Collectors.toSet());
    static final LightString adj = new LightString("adj");
    static final LightString noun = new LightString("noun");

    static boolean partFilter(IntPair pair, IntStringMap is, StringStringMap langPart) {
        LightString s1 = is.get(pair.getI1());
        LightString s2 = is.get(pair.getI2());
        LightString part1 = langPart.get(s1);
        LightString part2 = langPart.get(s2);
        return !exc.contains(s1) && !exc.contains(s2) && noun.equals(part2) && (noun.equals(part1) || adj.equals(part1));
    }
}
