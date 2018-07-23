package net.ninjacat.cql.parser;

/**
 * Types of tokens which {@link CqlTokenizer} can distinguish
 */
public enum TokenType {
    WHITESPACE,
    KEYWORD,
    STRING,
    NUMBER,
    UUID,
    SYMBOL,
    SEMICOLON,
    TYPE,
    SHELL,
    ID,
    GENERIC
}
