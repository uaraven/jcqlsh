package net.ninjacat.cql;

import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;

import java.util.List;
import java.util.StringTokenizer;

public class CqlParser implements Parser {

    private class Parsed implements ParsedLine {

        @Override
        public String word() {
            return null;
        }

        @Override
        public int wordCursor() {
            return 0;
        }

        @Override
        public int wordIndex() {
            return 0;
        }

        @Override
        public List<String> words() {
            return null;
        }

        @Override
        public String line() {
            return null;
        }

        @Override
        public int cursor() {
            return 0;
        }
    }

    @Override
    public ParsedLine parse(final String line, final int cursor, final ParseContext context) throws SyntaxError {

        return null;
    }


}
