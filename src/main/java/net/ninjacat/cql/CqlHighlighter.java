package net.ninjacat.cql;

import com.google.common.collect.ImmutableMap;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CqlHighlighter implements Highlighter {

    private static final Map<CqlTokenizer.TokenType, AttributedStyle> HIGHLIGHER =
            ImmutableMap.<CqlTokenizer.TokenType, AttributedStyle>builder()
                    .put(CqlTokenizer.TokenType.KEYWORD, new AttributedStyle().foreground(AttributedStyle.WHITE).bold())
                    .put(CqlTokenizer.TokenType.STRING, new AttributedStyle().foreground(AttributedStyle.CYAN))
                    .put(CqlTokenizer.TokenType.NUMBER, new AttributedStyle().foreground(AttributedStyle.YELLOW))
                    .put(CqlTokenizer.TokenType.UUID, new AttributedStyle().foreground(AttributedStyle.GREEN))
                    .build();


    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        final List<AttributedString> items = CqlTokenizer.parse(buffer, 0).stream()
                .map(this::tokenToAs)
                .collect(Collectors.toList());

        return AttributedString.join(new AttributedString(""), items);
    }

    private AttributedString tokenToAs(CqlTokenizer.Token token) {
        return new AttributedString(token.getToken(), HIGHLIGHER.getOrDefault(token.getTokenType(), AttributedStyle.DEFAULT));
    }
}
