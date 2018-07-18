package net.ninjacat.cql;

import net.ninjacat.cql.parser.CqlTokenizer;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.TokenType;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CqlParser implements Parser {

    public CqlParser() {
    }


    @Override
    public ParsedLine parse(final String line, final int cursor, final ParseContext context) throws SyntaxError {
        final List<Token> tokens = CqlTokenizer.parse(line, cursor);

        final Optional<Token> word = tokens.stream().filter(token -> token.getCursorPos() >= 0).findFirst();

        return new ParsedLine() {
            @Override
            public String word() {
                return word.map(Token::getToken).orElse(null);
            }

            @Override
            public int wordCursor() {
                return word.map(Token::getCursorPos).orElse(-1);
            }

            @Override
            public int wordIndex() {
                return word.map(Token::getIndex).orElse(-1);
            }

            @Override
            public List<String> words() {
                return tokens.stream()
                        .filter(it -> it.getTokenType() != TokenType.WHITESPACE)
                        .map(Token::getToken)
                        .collect(Collectors.toList());
            }

            @Override
            public String line() {
                return line;
            }

            @Override
            public int cursor() {
                return cursor;
            }
        };
    }


}
