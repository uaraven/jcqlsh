package net.ninjacat.cql;

import net.ninjacat.cql.parser.CqlTokenizer;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.TokenType;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static net.ninjacat.cql.parser.TokenType.*;
import static org.junit.Assert.assertThat;

public class CqlTokenizerTest {

    @Test
    public void shouldTokenizeBasicQuery() {
        final List<Token> tokens = CqlTokenizer.parse("select * from keyspace_name.table_name;");

        assertThat(tokens, Matchers.hasSize(8));
        assertThat(tokens.stream().map(Token::getTokenType).collect(Collectors.toList()),
                Matchers.contains(KEYWORD, WHITESPACE, SYMBOL, WHITESPACE,
                        KEYWORD, WHITESPACE, GENERIC, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithNumericCondition() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where id > 12;");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, NUMBER, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithUUID() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where id >= 731c882a-5035-4e7a-bfe8-a06fa8a31d90;");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, SYMBOL, UUID, SEMICOLON));
    }

    @Test
    public void shouldTokenizeString() {
        final List<Token> tokens = CqlTokenizer.parse("'test'");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(STRING));
    }

    @Test
    public void shouldTokenizeQueryWithString() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = 'test';");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithStringAndSpace() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = 'test is just a test';");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithStringAndOtherQuote() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = 'test is \"just\" a test';");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithStringAndYetOtherQuote() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = \"test is 'just' a test\";");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithStringAndEscapedQuote() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = \"test is \"\"just a test\";");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING, STRING, SEMICOLON));
    }

    @Test
    public void shouldTokenizeQueryWithUnterminatedString() {
        final List<Token> tokens = CqlTokenizer.parse("select * from table_name where name = \"test is ;");

        assertThat(getTokenTypes(tokens),
                Matchers.contains(KEYWORD, SYMBOL, KEYWORD, GENERIC, KEYWORD, GENERIC, SYMBOL, STRING));
    }

    private List<TokenType> getTokenTypes(List<Token> tokens) {
        return tokens.stream().map(Token::getTokenType).filter(t -> t != WHITESPACE).collect(Collectors.toList());
    }

}