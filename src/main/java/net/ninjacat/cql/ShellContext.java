package net.ninjacat.cql;

import com.datastax.driver.core.Session;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;

/**
 * Context of the shell. Contains terminal, Cassandra session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private final Terminal terminal;
    private final Session session;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
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
}
