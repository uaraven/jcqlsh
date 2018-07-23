package net.ninjacat.cql.parser;

import java.util.List;
import java.util.stream.Collectors;

public final class Tokens {

    private Tokens() {
    }

    public static List<Token> stripWhitespace(final List<Token> tokens) {
        return tokens.stream().filter(t -> t.getTokenType() != TokenType.WHITESPACE).collect(Collectors.toList());
    }
}
