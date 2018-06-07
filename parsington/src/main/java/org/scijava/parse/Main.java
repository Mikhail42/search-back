package org.scijava.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String... args) {
        String exp = "«Слово && о && полку && Игореве» / 5";
        SyntaxTree tree = new ExpressionParser().parseTree(exp);
        System.err.println(tree.toString());

        /*Pattern wordGroup = Pattern.compile("([()\\p{L}\u0301-]+)");
        Matcher m = wordGroup.matcher("(слово о полку)");
        if (m.find()) {
            System.err.println(m.group(1));
        }*/
    }
}
