package net.ninjacat.cql;

import org.jline.reader.Completer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class CqlShell {

    private final Terminal terminal;
    private final Completer completer;
    private final LineReader reader;
    private final History history;

    public CqlShell() throws IOException {
        this.terminal = TerminalBuilder.terminal();
        this.completer = createCqlCompleter();
        this.history = createHistory();

        this.reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(this.terminal)
                .completer(this.completer)
                .highlighter(new CqlHighlighter())
                .history(this.history)
                .build();
    }

    private static History createHistory() throws IOException {
        final DefaultHistory history = new DefaultHistory();
        history.load();
        return history;
    }

    private static Completer createCqlCompleter() {
        return new StringsCompleter("SELECT", "UPDATE", "DELETE", "FROM", "WHERE", "AND", "OR");
    }

    public void repl() {
        try {
            while (true) {
                String line = this.reader.readLine("#>");
            }
        } finally {
            try {
                this.history.save();
            } catch (final IOException ignored) {

            }
        }
    }

    public static void main(final String[] args) throws IOException {
        final CqlShell cqlShell = new CqlShell();
        cqlShell.repl();
    }
}
