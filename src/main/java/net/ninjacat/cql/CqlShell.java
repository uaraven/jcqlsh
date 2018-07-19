package net.ninjacat.cql;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import com.datastax.driver.core.Session;
import com.google.common.collect.Streams;
import net.ninjacat.cql.cassandra.CassandraProvider;
import net.ninjacat.cql.cassandra.CqlExecutor;
import net.ninjacat.cql.parser.CqlTokenizer;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.TokenType;
import net.ninjacat.cql.shell.ShellExecutor;
import net.ninjacat.cql.utils.Keywords;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CqlShell {

    private static final String MAIN_PROMPT = "> ";
    private static final String CONTINUATION_PROMPT = "... ";


    private final LineReader reader;
    private final History history;
    private final ShellExecutor shellExecutor;
    private final CqlExecutor cqlExecutor;
    private String prompt;

    private CqlShell(Parameters parameters) throws Exception {
        final Terminal terminal = TerminalBuilder.terminal();
        this.history = createHistory();

        this.reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(terminal)
                .completer(createCqlCompleter())
                .highlighter(new CqlHighlighter())
                .history(this.history)
                .build();

        final CassandraProvider cassandraProvider = new CassandraProvider();
        Session session = cassandraProvider.createSession(parameters, terminal);

        this.cqlExecutor = new CqlExecutor(session, terminal);
        this.shellExecutor = new ShellExecutor(session, terminal);
    }

    private static History createHistory() throws IOException {
        final DefaultHistory history = new DefaultHistory();
        history.load();
        return history;
    }

    private static Completer createCqlCompleter() {
        final List<String> recognizedTokens = Streams.concat(
                Keywords.readResource("/keywords").stream(),
                Keywords.readResource("/shell").stream()).collect(Collectors.toList());
        return new StringsCompleter(recognizedTokens);
    }

    public static void main(final String[] args) {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        final Parameters connectionParameters = new Parameters();
        final JCommander jc = JCommander.newBuilder()
                .addObject(connectionParameters)
                .build();
        jc.parse(args);

        try {
            final CqlShell cqlShell = new CqlShell(connectionParameters);
            cqlShell.repl();
        } catch (Exception ex) {
            System.err.println("Terminated with error: " + ex.getMessage());
        }
    }

    private void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    private void repl() {
        setPrompt(MAIN_PROMPT);
        try {
            final StringBuilder cmdBuilder = new StringBuilder();

            while (true) {
                final String line = this.reader.readLine(prompt);

                final List<Token> parsed = CqlTokenizer.parse(cmdBuilder.toString() + line)
                        .stream()
                        .filter(t -> t.getTokenType() != TokenType.WHITESPACE)
                        .collect(Collectors.toList());

                if (parsed.get(parsed.size() - 1).getTokenType() != TokenType.SEMICOLON) {
                    setPrompt(CONTINUATION_PROMPT);
                    cmdBuilder.append(line);
                } else {
                    setPrompt(MAIN_PROMPT);
                    cmdBuilder.setLength(0);

                    if (parsed.size() > 0) {
                        final String command = parsed.get(0).getToken();
                        if (isShellCommand(command)) {
                            this.shellExecutor.execute(parsed);
                        } else {
                            this.cqlExecutor.execute(parsed);
                        }
                    }

                }

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

    private boolean isShellCommand(String command) {
        return this.shellExecutor.isShellCommand(command);
    }
}
