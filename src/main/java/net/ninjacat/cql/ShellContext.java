package net.ninjacat.cql;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import net.ninjacat.cql.printer.ResultSetPrinterType;
import net.ninjacat.cql.shell.ShellException;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Context of the shell. Contains terminal, KeyspaceTable session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private static final int DEFAULT_PAGE_SIZE = 25;
    private final Terminal terminal;
    private final Session session;
    private ResultSetPrinterType resultSetPrinter;
    private int paging;
    private boolean tracingEnabled;

    private ConsistencyLevel consistencyLevel;
    private ConsistencyLevel serialConsistencyLevel;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
        this.consistencyLevel = ConsistencyLevel.ONE;
        this.serialConsistencyLevel = ConsistencyLevel.SERIAL;
        this.tracingEnabled = false;
        this.resultSetPrinter = ResultSetPrinterType.FLAT;
        this.paging = "dumb".equals(terminal.getType()) ? DEFAULT_PAGE_SIZE : terminal.getHeight();
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

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public ConsistencyLevel getSerialConsistencyLevel() {
        return this.serialConsistencyLevel;
    }

    public void setSerialConsistencyLevel(ConsistencyLevel serialConsistencyLevel) {
        this.serialConsistencyLevel = serialConsistencyLevel;
    }

    public ResultSetPrinterType getResultSetPrinter() {
        return this.resultSetPrinter;
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.resultSetPrinter = resultSetPrinter;
    }

    public int getPaging() {
        return this.paging;
    }

    public void setPaging(final int paging) {
        this.paging = paging;
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
