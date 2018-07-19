package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.Session;
import net.ninjacat.cql.parser.Token;
import org.jline.terminal.Terminal;

import java.util.List;

public class CqlExecutor {

    private final Session session;

    public CqlExecutor(final Session session, Terminal terminal) {
        this.session = session;
    }

    public void execute(List<Token> line) {

    }
}
