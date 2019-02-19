package net.ninjacat.cql.copy;

import net.ninjacat.cql.CqlCopyLexer;
import net.ninjacat.cql.CqlCopyParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CopyBuilderTest {

    @Test
    public void shouldParseSimpleCopyTo() {
        final String text = "COPY table(col1, col2) TO '/dev/null'";
        final CqlCopyData copyData = parse(text);

        assertThat(copyData.getTableName(), is("table"));
        assertThat(copyData.getColumnNames(), contains("col1", "col2"));
        assertThat(copyData.getDirection(), is(CopyDirection.TO));
        assertThat(copyData.isUseConsoleForIo(), is(false));
        assertThat(copyData.getFileName(), contains("/dev/null"));
    }

    @Test
    public void shouldParseSimpleCopyToStdio() {
        final String text = "COPY table(col1, col2) TO stdout";
        final CqlCopyData copyData = parse(text);

        assertThat(copyData.getTableName(), is("table"));
        assertThat(copyData.getColumnNames(), contains("col1", "col2"));
        assertThat(copyData.getDirection(), is(CopyDirection.TO));
        assertThat(copyData.isUseConsoleForIo(), is(true));
        assertThat(copyData.getFileName(), hasSize(0));
    }

    @Test
    public void shouldParseSimpleCopyFrom() {
        final String text = "COPY table(col1, col2) FROM '/dev/null'";
        final CqlCopyData copyData = parse(text);

        assertThat(copyData.getTableName(), is("table"));
        assertThat(copyData.getColumnNames(), contains("col1", "col2"));
        assertThat(copyData.getDirection(), is(CopyDirection.FROM));
        assertThat(copyData.isUseConsoleForIo(), is(false));
        assertThat(copyData.getFileName(), contains("/dev/null"));
    }

    @Test
    public void shouldParseSimpleCopyFromStdin() {
        final String text = "COPY table(col1, col2) FROM stdin";
        final CqlCopyData copyData = parse(text);

        assertThat(copyData.getTableName(), is("table"));
        assertThat(copyData.getColumnNames(), contains("col1", "col2"));
        assertThat(copyData.getDirection(), is(CopyDirection.FROM));
        assertThat(copyData.isUseConsoleForIo(), is(true));
        assertThat(copyData.getFileName(), hasSize(0));
    }

    private static CqlCopyData parse(final String text) {
        final CqlCopyLexer cqlCopyLexer = new CqlCopyLexer(CharStreams.fromString(text));
        final CqlCopyParser cqlCopyParser = new CqlCopyParser(new CommonTokenStream(cqlCopyLexer));
        final ParseTreeWalker walker = new ParseTreeWalker();
        final CopyBuilder copyBuilder = new CopyBuilder();
        walker.walk(copyBuilder, cqlCopyParser.copy_stmt());

        return copyBuilder.getCopyData();
    }
}