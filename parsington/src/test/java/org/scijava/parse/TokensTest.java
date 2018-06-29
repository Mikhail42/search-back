package org.scijava.parse;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokensTest {

    @Test
    public void isGroup() {
        SyntaxTree andThree = new ExpressionParser().parseTree("a && b");//new SyntaxTree("a && b");
        SyntaxTree quote = new ExpressionParser().parseTree("«a && b»");
        int a = 5;
    }
}