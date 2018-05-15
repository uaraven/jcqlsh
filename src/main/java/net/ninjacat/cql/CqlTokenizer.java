package net.ninjacat.cql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

public class CqlTokenizer {

    private static final String DELIM = " \t\"'\n\r;,.()[]<>=?";
    private static final Set<String> SYMBOLS = Sets.newHashSet(",", ".", "(", ")", "[", "]", "<", ">", "=", "?");
    private static final Set<String> KEYWORDS = Sets.newHashSet("SELECT", "UPDATE", "DELETE", "FROM", "WHERE", "SET", "DESC", "SHOW");


    public static List<Token> parse(final String line, final int cursorPos) {
        final StringTokenizer tokenizer = new StringTokenizer(line, DELIM, true);

        final ImmutableList.Builder<Token> result = ImmutableList.builder();

        String currentDelim = DELIM;
        String currentQuote = "";
        String prevToken = "";
        while (true) {
            final String token = tokenizer.nextToken(currentDelim);
            if (currentQuote.isEmpty() && ("'".equals(token) || "\"".equals(token))) {
                currentQuote = token;
                currentDelim = token + "\n";
            } else {
                currentDelim = DELIM;

                if (token.equals(currentQuote)) {
                    result.add(new Token(currentQuote + prevToken + currentQuote, TokenType.STRING, -1));
                    currentQuote = "";
                } else {
                    result.add(new Token(token, guessTokenType(token), -1));
                }
            }
            prevToken = token;
        }
    }

    private static TokenType guessTokenType(final String token) {
        if (SYMBOLS.contains(token)) {
            return TokenType.SYMBOL;
        } else if (KEYWORDS.contains(token)) {
            return TokenType.KEYWORD;
        } else if (";".equals(token)) {
            return TokenType.SEMICOLON;
        }
        try {
            UUID.fromString(token);
            return TokenType.UUID;
        } catch (final Exception ignored) {
        }
        try {
            Double.parseDouble(token);
            return TokenType.NUMBER;
        } catch (final Exception ignored) {
        }
        return TokenType.GENERIC;
    }

    public static class Token {
        private final String token;
        private final TokenType tokenType;
        private final int cursorPos;

        Token(final String token, final TokenType type, final int cursorPos) {
            this.token = token;
            this.tokenType = type;
            this.cursorPos = cursorPos;
        }

        public int getCursorPos() {
            return this.cursorPos;
        }

        public String getToken() {
            return this.token;
        }

        public TokenType getTokenType() {
            return this.tokenType;
        }
    }

    public enum TokenType {
        KEYWORD,
        STRING,
        NUMBER,
        UUID,
        SYMBOL,
        SEMICOLON,
        GENERIC
    }
}
