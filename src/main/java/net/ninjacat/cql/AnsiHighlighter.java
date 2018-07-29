package net.ninjacat.cql;

import com.google.common.collect.ImmutableMap;
import net.ninjacat.cql.parser.CqlTokenizer;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.TokenType;
import org.fusesource.jansi.Ansi;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Highlights CQL queries for ansi output
 */
public final class AnsiHighlighter {

    private static final Map<TokenType, Consumer<Ansi>> HIGHLIGHTER =
            ImmutableMap.<TokenType, Consumer<Ansi>>builder()
                    .put(TokenType.KEYWORD, ansi -> ansi.a(Ansi.Attribute.INTENSITY_BOLD))
                    .put(TokenType.SHELL, ansi -> ansi.a(Ansi.Attribute.ITALIC))
                    .put(TokenType.SYMBOL, Ansi::fgDefault)
                    .put(TokenType.STRING, ansi -> ansi.fgBlue().a(Ansi.Attribute.INTENSITY_BOLD))
                    .put(TokenType.NUMBER, Ansi::fgYellow)
                    .put(TokenType.UUID, Ansi::fgGreen)
                    .put(TokenType.ID, Ansi::fgMagenta)
                    .put(TokenType.TYPE, ansi -> ansi.fgCyan().a(Ansi.Attribute.INTENSITY_BOLD))
                    .build();

    private AnsiHighlighter() {
    }


    public static Ansi highlight(final String cql) {
        final Ansi ansi = Ansi.ansi();

        CqlTokenizer.parse(cql, 0).forEach(token -> {
            tokenToAs(token).accept(ansi);
            ansi.render(token.getToken());
            ansi.reset();
        });

        return ansi;
    }

    private static Consumer<Ansi> tokenToAs(final Token token) {
        return HIGHLIGHTER.getOrDefault(token.getTokenType(), ansi -> {
        });
    }

}
