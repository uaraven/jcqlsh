package net.ninjacat.cql;

import com.google.common.collect.Streams;
import net.ninjacat.cql.utils.Keywords;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CqlShell {

    private final Terminal terminal;
    private final LineReader reader;
    private final History history;

    public CqlShell() throws IOException {
        this.terminal = TerminalBuilder.terminal();
        this.history = createHistory();

        this.reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(this.terminal)
                .completer(createCqlCompleter())
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
        final List<String> recognizedTokens = Streams.concat(Streams.concat(
                Keywords.readResource("/keywords").stream(),
                Keywords.readResource("/shell").stream()),
                Keywords.readResource("/types").stream()).collect(Collectors.toList());
        return new StringsCompleter(recognizedTokens);
    }

    private void repl() {
        try {
            while (true) {
                String line = this.reader.readLine("#> ");
            }
        } catch (UserInterruptException | EndOfFileException ignored) {
            System.out.println("\nDone");
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
