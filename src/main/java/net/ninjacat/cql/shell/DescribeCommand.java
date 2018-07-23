package net.ninjacat.cql.shell;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;
import net.ninjacat.cql.utils.KeyspaceTable;
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
            if (filteredTokens.size() < 2) {
                throw new DescribeException("Parameter expected");
            }
            switch (filteredTokens.get(1).getToken().toLowerCase()) {
                case "keyspaces":
                    listKeyspaces(context);
                    break;
                case "tables":
                    listTables(context);
                    break;
                case "keyspace":
                    try {
                        final String keyspaceName = filteredTokens.get(2).getToken();
                        listKeyspace(context, keyspaceName);
                    } catch (final Exception ex) {
                        throw new DescribeException(ex.getMessage(), ex.getCause());
                    }
                    break;
                case "table":
                    try {
                        final KeyspaceTable keyspaceName = KeyspaceTable.of(filteredTokens.get(2).getToken());
                        listTable(context, keyspaceName);
                    } catch (final Exception ex) {
                        throw new DescribeException(ex.getMessage(), ex.getCause());
                    }
                    break;
                default:
                    try {
                        final KeyspaceTable keyspaceName = KeyspaceTable.of(filteredTokens.get(1).getToken());
                        if (keyspaceName.hasKeyspace()) {
                            listTable(context, keyspaceName);
                        } else {
                            if (context.getSession().getCluster().getMetadata().getKeyspace(keyspaceName.getFullName()) == null) {
                                listTable(context, keyspaceName);
                            } else {
                                listKeyspace(context, keyspaceName.getFullName());
                            }
                        }
                    } catch (final Exception ex) {
                        throw new DescribeException(ex.getMessage(), ex.getCause());
                    }
            }
        } catch (final DescribeException ex) {
            context.writer().println(ansi().fgRed().a(String.format("Improper %s command: %s", tokens.get(0), ex.getMessage())).reset());
        }
    }

    private void listKeyspace(final ShellContext context, final String keyspaceName) {
        final KeyspaceMetadata keyspace = context.getSession().getCluster().getMetadata().getKeyspace(keyspaceName);
        if (keyspace == null) {
            throw new DescribeException("Unknown keyspace: " + keyspaceName);
        }
        context.writer().println(keyspace.exportAsString());
        context.writer().println();
    }

    private void listTable(final ShellContext context, final KeyspaceTable keyspTable) {
        final String keyspaceName = keyspTable.hasKeyspace() ? keyspTable.getKeyspace() : context.getSession().getLoggedKeyspace();
        if (keyspaceName == null || keyspaceName.isEmpty()) {
            throw new DescribeException("Keyspace not specified");
        }
        final KeyspaceMetadata keyspace = context.getSession().getCluster().getMetadata().getKeyspace(keyspaceName);
        final TableMetadata table = keyspace.getTable(keyspTable.getTable());
        context.writer().println(table.exportAsString());
        context.writer().println();
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
            for (final TableMetadata table : keyspace.getTables()) {
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

    private void listKeyspaces(final ShellContext context) {
        final String currentKeyspace = context.getSession().getLoggedKeyspace();
        final List<KeyspaceMetadata> keyspaces = context.getSession().getCluster().getMetadata().getKeyspaces();
        keyspaces.forEach(keyspaceMetadata -> {
            final Ansi ksps = keyspaceMetadata.getName().equals(currentKeyspace)
                    ? ansi().bgCyan().fgBrightYellow().a(currentKeyspace).reset()
                    : ansi().a(keyspaceMetadata.getName());
            context.writer().println(ksps);
        });
    }
}
