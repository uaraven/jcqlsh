package net.ninjacat.cql.shell;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.cassandra.CqlExecutor;
import net.ninjacat.cql.cassandra.CqlResultSetPrinter;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;
import net.ninjacat.cql.utils.JCqlSh;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

public class ShowCommand implements ShellCommand {

    private static Versions getCassandraVersion(final Session session) {
        final Row row = session.execute("select cql_version, release_version, native_protocol_version" +
                " from system.local where key = 'local'").one();
        return new Versions(row.getString(0), row.getString(1), row.getString(2));
    }

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            if (filteredTokens.size() < 2) {
                throw new ShellException("Not enough arguments. Usage:\n   show version|host|session|sessions");
            }
            switch (filteredTokens.get(1).getToken().toLowerCase()) {
                case "version":
                    final Versions v = getCassandraVersion(context.getSession());
                    context.writer().println(
                            ansi().a("jcqlsh ").fgYellow().a(JCqlSh.VERSION).reset()
                                    .a(" | Cassandra ").fgYellow().a(v.releaseVersion).reset()
                                    .a(" | CQL spec ").fgYellow().a(v.cqlVersion).reset()
                                    .a(" | Native protocol ").fgYellow().a("v" + v.nativeProtocolVersion).reset()
                    );
                    break;
                case "host":
                    final String clusterName = context.getSession().getCluster().getClusterName();
                    final String hosts = context.getSession().getState().getConnectedHosts().stream()
                            .map(h -> h.getAddress().toString()).collect(Collectors.joining(", "));
                    context.writer().println(ansi().a("Connected to ").fgBlue().a(clusterName).reset().a(" at ")
                            .fgYellow().a(hosts).reset());
                    break;
                case "session":
                    if (filteredTokens.size() < 3) {
                        throw new ShellException("Session id expected");
                    }
                    try {
                        final UUID sessionId = UUID.fromString(filteredTokens.get(2).getToken());
                        showSessions(context, sessionId);
                    } catch (final IllegalArgumentException ex) {
                        throw new ShellException("Session id must be UUID string", ex);
                    }
                    break;
                case "sessions":
                    new CqlExecutor(context).execute("select session_id from system_traces.sessions", false);
                    break;
                default:
                    throw new ShellException(String.format("Unknown argument '%s'", filteredTokens.get(1).getToken()));
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }

    }

    private static void showSessions(final ShellContext context, final UUID sessionId) {
        final BoundStatement statement = context.getSession()
                .prepare("select * from system_traces.sessions where session_id = %s")
                .bind(sessionId);
        final ResultSet resultSet = context.getSession().execute(statement);
        new CqlResultSetPrinter(context).printResultSet(resultSet);
    }

    private static final class Versions {
        final String cqlVersion;
        final String releaseVersion;
        final String nativeProtocolVersion;

        Versions(final String cqlVersion, final String releaseVersion, final String nativeProtocolVersion) {
            this.cqlVersion = cqlVersion;
            this.releaseVersion = releaseVersion;
            this.nativeProtocolVersion = nativeProtocolVersion;
        }
    }
}
