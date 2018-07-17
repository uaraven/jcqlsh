package net.ninjacat.cql;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

public class CqlHighlighter implements Highlighter {
    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        return new AttributedString(buffer);
    }
}
