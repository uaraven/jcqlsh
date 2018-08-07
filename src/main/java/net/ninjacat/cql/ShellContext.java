package net.ninjacat.cql;

import com.datastax.driver.core.Session;
import net.ninjacat.cql.printer.ResultSetPrinterType;
import net.ninjacat.cql.printer.ScreenSettings;
import net.ninjacat.cql.shell.ShellException;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Context of the shell. Contains terminal, KeyspaceTable session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private final Terminal terminal;
    private final Session session;
    private final ScreenSettings screenSettings;
    private boolean tracingEnabled;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
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

    public ResultSetPrinterType getResultSetPrinter() {
        return this.screenSettings.getResultSetPrinter();
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.screenSettings.setResultSetPrinter(resultSetPrinter);
    }

    public ScreenSettings getScreenSettings() {
        return this.screenSettings;
    }

    public void waitForKeypress() {
        try {
            int read;
            do {
                read = this.getTerminal().reader().read(Long.MAX_VALUE);
                if (read == -1) {
                    throw new ShellException("Terminated");
                }
            } while (read < 0);
        } catch (final IOException e) {
            throw new ShellException("", e);
        }
    }
}
