package net.ninjacat.cql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.regex.Pattern;

class CqlTokenizer {
    private static final Pattern WS_PATTERN = Pattern.compile("\\s+");

    private static final String DELIM = " \t\"'\n\r;,.()[]<>=?";
    private static final Set<String> SYMBOLS = Sets.newHashSet(",", ".", "(", ")", "[", "]", "<", ">", "=", "?");
    private static final Set<String> KEYWORDS = Sets.newHashSet("SELECT", "UPDATE", "DELETE", "FROM", "WHERE", "SET", "DESC", "SHOW");


    static List<Token> parse(final String line, final int cursorPos) {
        final StringTokenizer tokenizer = new StringTokenizer(line, DELIM, true);
        final ImmutableList.Builder<Token> result = ImmutableList.builder();

        String currentDelim = DELIM;
        String currentQuote = "";
        String prevToken = "";
        int position = 0;
        int tokenIndex = 0;
        try {
            while (true) {
                final String token = tokenizer.nextToken(currentDelim);
                if (currentQuote.isEmpty() && ("'".equals(token) || "\"".equals(token))) {
                    currentQuote = token;
                    currentDelim = token + "\n";
                } else {
                    currentDelim = DELIM;

                    int cursorInWord;
                    if (cursorPos >= position && cursorPos <= (position + token.length())) {
                        cursorInWord = cursorPos - position;
                    } else {
                        cursorInWord = -1;
                    }

                    if (token.equals(currentQuote)) {
                        result.add(new Token(tokenIndex, currentQuote + prevToken + currentQuote, TokenType.STRING, cursorInWord));
                        currentQuote = "";
                    } else {
                        result.add(new Token(tokenIndex, token, guessTokenType(token), cursorInWord));
                    }
                    tokenIndex += 1;
                    position += token.length();
                }
                prevToken = token;
            }
        } catch (final NoSuchElementException ex) {
            return result.build();
        }
    }

    private static TokenType guessTokenType(final String token) {
        if (WS_PATTERN.matcher(token).matches()) {
            return TokenType.WHITESPACE;
        }
        if (SYMBOLS.contains(token)) {
            return TokenType.SYMBOL;
        } else if (KEYWORDS.contains(token.toUpperCase())) {
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

    public enum TokenType {
        WHITESPACE,
        KEYWORD,
        STRING,
        NUMBER,
        UUID,
        SYMBOL,
        SEMICOLON,
        GENERIC
    }

    static class Token {
        private final int index;
        private final String token;
        private final TokenType tokenType;
        private final int cursorPos;

        Token(final int index, final String token, final TokenType type, final int cursorPos) {
            this.index = index;
            this.token = token;
            this.tokenType = type;
            this.cursorPos = cursorPos;
        }

        int getIndex() {
            return index;
        }

        int getCursorPos() {
            return this.cursorPos;
        }

        String getToken() {
            return this.token;
        }

        TokenType getTokenType() {
            return this.tokenType;
        }
    }
}
