package net.ninjacat.cql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import static net.ninjacat.cql.utils.Keywords.readResource;

/**
 * Dumb tokenizer for CQL queries.
 * <p>
 * Does not understand semantics
 */
public class CqlTokenizer {
    private static final String DELIM = " \t\"'\n\r;,()[]<>=?`";
    private static final Set<String> WHITESPACE = ImmutableSet.of(" ", "\t", "\n", "\r");
    private static final Set<String> SYMBOLS = ImmutableSet.of(",", "(", ")", "[", "]", "<", ">", "=", "?", "*");
    private static final Set<String> KEYWORDS = readResource("/keywords");
    private static final Set<String> TYPES = readResource("/types");
    private static final Set<String> SHELL = readResource("/shell");

    private CqlTokenizer() {
    }

    public static List<Token> parse(final String line) {
        return parse(line, -1);
    }

    public static List<Token> parse(final String line, final int cursorPos) {
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
                if (currentQuote.isEmpty() && ("'".equals(token) || "\"".equals(token) || "`".equals(token))) {
                    currentQuote = token;
                    currentDelim = token + "\n";
                } else {
                    int cursorInWord;
                    if (cursorPos >= position && cursorPos <= (position + token.length())) {
                        cursorInWord = cursorPos - position;
                    } else {
                        cursorInWord = -1;
                    }

                    if (token.equals(currentQuote)) {
                        final TokenType type = "`".equals(token) ? TokenType.ID : TokenType.STRING;
                        result.add(new Token(tokenIndex, currentQuote + prevToken + currentQuote, type, cursorInWord));
                        currentDelim = DELIM;
                        currentQuote = "";
                    } else if (currentQuote.isEmpty()) {
                        result.add(new Token(tokenIndex, token, guessTokenType(token), cursorInWord));
                    }
                    tokenIndex += 1;
                    position += token.length();
                }
                prevToken = token;
            }
        } catch (final NoSuchElementException ex) {
            if (!currentQuote.isEmpty()) { // handle unterminated string
                result.add(new Token(tokenIndex, currentQuote + prevToken, TokenType.STRING, -1));
            }
            return result.build();
        }
    }

    private static TokenType guessTokenType(final String token) {
        if (WHITESPACE.contains(token)) {
            return TokenType.WHITESPACE;
        } else if (SYMBOLS.contains(token)) {
            return TokenType.SYMBOL;
        } else if (KEYWORDS.contains(token.toUpperCase())) {
            return TokenType.KEYWORD;
        } else if (SHELL.contains(token.toUpperCase())) {
            return TokenType.SHELL;
        } else if (TYPES.contains(token.toUpperCase())) {
            return TokenType.TYPE;
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

}
