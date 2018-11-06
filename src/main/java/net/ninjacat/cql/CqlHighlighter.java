package net.ninjacat.cql;

import com.google.common.collect.ImmutableMap;
import net.ninjacat.cql.parser.CqlTokenizer;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.TokenType;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CqlHighlighter implements Highlighter {

    private static final Map<TokenType, AttributedStyle> HIGHLIGHTER =
            ImmutableMap.<TokenType, AttributedStyle>builder()
                    .put(TokenType.KEYWORD, new AttributedStyle().foreground(AttributedStyle.BRIGHT + AttributedStyle.YELLOW))
                    .put(TokenType.SHELL, new AttributedStyle().foreground(AttributedStyle.WHITE).italic())
                    .put(TokenType.SYMBOL, new AttributedStyle().foreground(AttributedStyle.WHITE))
                    .put(TokenType.STRING, new AttributedStyle().foreground(AttributedStyle.BLUE + AttributedStyle.BRIGHT))
                    .put(TokenType.NUMBER, new AttributedStyle().foreground(AttributedStyle.MAGENTA))
                    .put(TokenType.UUID, new AttributedStyle().foreground(AttributedStyle.GREEN))
                    .put(TokenType.ID, new AttributedStyle().foreground(AttributedStyle.MAGENTA))
                    .put(TokenType.TYPE, new AttributedStyle().foreground(AttributedStyle.CYAN).bold())
                    .build();


    @Override
    public AttributedString highlight(final LineReader reader, final String buffer) {
        final List<AttributedString> items = CqlTokenizer.parse(buffer, 0).stream()
                .map(CqlHighlighter::highlightToken)
                .collect(Collectors.toList());

        return AttributedString.join(new AttributedString(""), items);
    }

    public static AttributedString highlightToken(final Token token) {
        return new AttributedString(token.getToken(), HIGHLIGHTER.getOrDefault(token.getTokenType(), AttributedStyle.DEFAULT));
    }

}
