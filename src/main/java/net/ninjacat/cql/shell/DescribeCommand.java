package net.ninjacat.cql.shell;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.Collections;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Implementation of 'describe' command.
 * <pre>
 *     Supports following parameters:
 *     - desc keyspaces - shows list of all keyspaces
 *     - desc tables - shows list of all tables in current keyspace or list of all tables across all keyspaces
 *     - desc [keyspace] "keyspace_name" - shows CQL create statements for keyspace
 *     - desc [table] "table_name" shows CQL create statement for table. "table_name" can be keyspace.table
 * </pre>
 */
public class DescribeCommand implements ShellCommand {

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            switch (filteredTokens.get(1).getToken().toLowerCase()) {
                case "keyspaces":
                    listKeyspaces(context);
                    break;
                case "tables":
                    listTables(context);
                    break;
                case "table":
                    break;
            }
        } catch (final DescribeException ex) {
            context.writer().println(ansi().fgRed().a(String.format("Improper %s command", tokens.get(0))).reset());
        }
    }

    private void listTables(final ShellContext context) {
        final String currentKeyspace = context.getSession().getLoggedKeyspace();
        if (currentKeyspace == null) {
            context.getSession().getCluster().getMetadata().getKeyspaces().forEach(k -> {
                context.writer().println();
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
        final int maxWidth = context.getTerminal().getWidth();
        context.writer().println();
        if (keyspace.getTables().isEmpty()) {
            context.writer().println("<empty>");
        } else {
            final int columnWidth = keyspace.getTables().stream().map(tm -> tm.getName().length()).max(Integer::compareTo).orElse(30) + 4;
            int currentWidth = 0;
            for (TableMetadata table : keyspace.getTables()) {
                context.writer().print(StringUtils.rightPad(table.getName(), columnWidth));
                currentWidth += columnWidth;
                if (currentWidth + columnWidth >= maxWidth) {
                    currentWidth = 0;
                    context.writer().println();
                }
            }
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
