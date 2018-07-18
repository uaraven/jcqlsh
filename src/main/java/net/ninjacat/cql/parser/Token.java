package net.ninjacat.cql.parser;

public class Token {
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

    public int getIndex() {
        return index;
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
