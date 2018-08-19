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
import net.ninjacat.cql.utils.Utils;
import net.ninjacat.smooth.utils.Try;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class CqlShell implements Closeable, AutoCloseable {

    private static final String MAIN_PROMPT = "jcql> ";
    private static final String CONTINUATION_PROMPT = " ... ";

    private final LineReader reader;
    private final History history;
    private final ShellExecutor shellExecutor;
    private final CqlExecutor cqlExecutor;
    private final ShellContext context;
    private String prompt;

    private CqlShell(final Parameters parameters) throws Exception {

        final Terminal terminal = Try.execute(() -> buildDefaultTerminal(parameters))
                .recover(CqlShell::buildFallbackTerminal)
                .getValue();
        this.history = createHistory();

        this.reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(terminal)
                .completer(createCqlCompleter())
                .highlighter(new CqlHighlighter())
                .history(this.history)
                .build();

        final CassandraProvider cassandraProvider = new CassandraProvider();
        final Session session = cassandraProvider.createSession(parameters, terminal);

        this.context = new ShellContext(terminal, session);

        this.cqlExecutor = new CqlExecutor(this.context);
        this.shellExecutor = new ShellExecutor(this.context);
    }

    /**
     * Default terminal that should work fine on Linux and MacOS
     *
     * @param parameters
     * @return
     * @throws IOException
     */
    private static Terminal buildDefaultTerminal(final Parameters parameters) throws IOException {
        return TerminalBuilder.builder()
                .dumb(isDumb(parameters))
                .build();
    }

    /**
     * Default terminal fails to create on Windows in CDM.EXE or Powershell (but works fine in cygwin or msys). \
     * This method will attempt to create dumb non-system terminal that works in CMD and Powershell
     *
     * @return Terminal
     * @throws IOException
     */
    private static Terminal buildFallbackTerminal() throws IOException {
        return TerminalBuilder.builder()
                .dumb(true)
                .system(false)
                .build();
    }

    private static boolean isDumb(final Parameters parameters) {
        return System.console() == null || parameters.isNoColor();
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

        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        final Parameters connectionParameters = new Parameters();
        final JCommander jc = JCommander.newBuilder()
                .addObject(connectionParameters)
                .build();
        jc.parse(args);

        try (final CqlShell cqlShell = new CqlShell(connectionParameters)) {
            cqlShell.repl();
        } catch (final Exception ex) {
            System.err.println("Terminated with error: " + ex.getMessage());
            System.exit(0);
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
                setPrompt(this.prompt);
                final String line;
                try {
                    line = this.reader.readLine(this.prompt);
                } catch (final UserInterruptException ignored) {
                    continue;
                }
                if (line == null) {
                    throw new EndOfFileException();
                }

                final List<Token> parsed = CqlTokenizer.parse(cmdBuilder.toString() + line);

                if (!parsed.isEmpty()) {
                    final String command = parsed.get(0).getToken();
                    if (isShellCommand(command)) {
                        this.shellExecutor.execute(parsed);
                        this.prompt = mainPrompt();
                    } else {
                        if (parsed.get(parsed.size() - 1).getTokenType() != TokenType.SEMICOLON) {
                            this.prompt = CONTINUATION_PROMPT;
                            cmdBuilder.append(line);
                        } else {
                            cmdBuilder.setLength(0);
                            this.cqlExecutor.execute(parsed);
                            this.prompt = mainPrompt();
                        }
                    }
                }

            }
        } catch (final EndOfFileException ignored) {
            System.out.println("\nDone");
            Utils.closeQuietly(this.context.getTerminal());
            System.exit(0);
        } finally {
            try {
                this.history.save();
            } catch (final IOException ignored) {
            }
        }
    }

    private String mainPrompt() {
        if (this.context.getSession().getLoggedKeyspace() != null) {
            return String.format("jcql:%s> ", this.context.getSession().getLoggedKeyspace());
        } else {
            return MAIN_PROMPT;
        }
    }

    private static boolean isShellCommand(final String command) {
        return ShellExecutor.isShellCommand(command);
    }

    @Override
    public void close() {
        Utils.closeQuietly(this.context.getTerminal());
    }
}
