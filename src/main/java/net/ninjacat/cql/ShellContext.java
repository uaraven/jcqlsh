package net.ninjacat.cql;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import net.ninjacat.cql.printer.ResultSetPrinterType;
import net.ninjacat.cql.printer.ScreenSettings;
import net.ninjacat.cql.shell.ShellException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;

/**
 * Context of the shell. Contains terminal, KeyspaceTable session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private final Terminal terminal;
    private final Session session;
    private final ScreenSettings screenSettings;
    private boolean tracingEnabled;

    private ConsistencyLevel consistencyLevel;
    private ConsistencyLevel serialConsistencyLevel;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
        this.consistencyLevel = ConsistencyLevel.ONE;
        this.serialConsistencyLevel = ConsistencyLevel.SERIAL;
        this.tracingEnabled = false;
        this.screenSettings = new ScreenSettings(ResultSetPrinterType.COMPACT, terminal.getType().startsWith("dumb") ? 40 : terminal.getHeight());
    }

    public boolean isRunningInTerminal() {
        return System.console() != null;
    }

    public PrintWriter writer() {
        return this.terminal.writer();
    }

    public Terminal getTerminal() {
        return this.terminal;
    }

    public Session getSession() {
        return this.session;
    }

    public boolean isTracingEnabled() {
        return this.tracingEnabled;
    }

    public void setTracingEnabled(final boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    public void setConsistencyLevel(final ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public ConsistencyLevel getSerialConsistencyLevel() {
        return this.serialConsistencyLevel;
    }

    public void setSerialConsistencyLevel(final ConsistencyLevel serialConsistencyLevel) {
        this.serialConsistencyLevel = serialConsistencyLevel;
    }

    public boolean isPagingEnabled() {
        return this.screenSettings.isPagingEnabled();
    }

    public void setPagingEnabled(final boolean pagingEnabled) {
        this.screenSettings.setPagingEnabled(pagingEnabled);
    }

    public ResultSetPrinterType getResultSetPrinter() {
        return this.screenSettings.getResultSetPrinter();
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.screenSettings.setResultSetPrinter(resultSetPrinter);
    }

    public ScreenSettings getScreenSettings() {
        return this.screenSettings;
    }

    public boolean waitForKeypress() {
        final LineReader reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(getTerminal())
                .build();
        try {
            reader.readLine((char) 0);
            return true;
        } catch (final UserInterruptException ex) {
            return false;
        } catch (final Exception e) {
            throw new ShellException("", e);
        }
    }
}
