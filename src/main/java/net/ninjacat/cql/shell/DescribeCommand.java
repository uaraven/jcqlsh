package net.ninjacat.cql.shell;

import com.datastax.driver.core.KeyspaceMetadata;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import org.fusesource.jansi.Ansi;

import java.util.Collections;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class DescribeCommand implements ShellCommand {

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {

        switch (tokens.get(0).getToken().toLowerCase()) {
            case "keyspaces":
                listKeyspaces(context);
                break;
            case "tables":
                listTables(context);
                break;
            case "table":
                break;
            default:
                context.writer().println(ansi().fg(Ansi.Color.RED).a("Improper desc command").reset());
        }
    }

    private void listTables(final ShellContext context) {
        final String currentKeyspace = context.getSession().getLoggedKeyspace();
        if (currentKeyspace == null) {
            context.getSession().getCluster().getMetadata().getKeyspaces().forEach(k -> {
                context.writer().println(k.getName());
                context.writer().println(String.join("", Collections.nCopies(k.getName().length(), "-")));

                listTablesForKeyspace(context, k.getName());
            });
        } else {
            listTablesForKeyspace(context, currentKeyspace);
        }
    }

    private void listTablesForKeyspace(final ShellContext context, final String keyspaceName) {
        final KeyspaceMetadata keyspace = context.getSession().getCluster().getMetadata().getKeyspace(keyspaceName);
        context.writer().println();
        if (keyspace.getTables().isEmpty()) {
            context.writer().println("<empty>");
        } else {
            keyspace.getTables().forEach(t -> context.writer().println(t.getName()));
        }
        context.writer().println();
    }

    private static void listKeyspaces(final ShellContext context) {
        final String currentKeyspace = context.getSession().getLoggedKeyspace();
        final List<KeyspaceMetadata> keyspaces = context.getSession().getCluster().getMetadata().getKeyspaces();
        keyspaces.forEach(keyspaceMetadata -> {
            Ansi ksps = keyspaceMetadata.getName().equals(currentKeyspace)
                    ? ansi().bgCyan().fgBrightYellow().a(currentKeyspace).reset()
                    : ansi().a(keyspaceMetadata.getName());
            context.writer().println(ksps);
        });
    }
}
