package net.ninjacat.cql;

import com.datastax.driver.core.Session;
import net.ninjacat.cql.printer.ResultSetPrinterType;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;

/**
 * Context of the shell. Contains terminal, KeyspaceTable session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private final Terminal terminal;
    private final Session session;
    private ResultSetPrinterType resultSetPrinter;
    private boolean tracingEnabled;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
        this.tracingEnabled = false;
        this.resultSetPrinter = ResultSetPrinterType.TABLE;
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
        return this.resultSetPrinter;
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.resultSetPrinter = resultSetPrinter;
    }
}
