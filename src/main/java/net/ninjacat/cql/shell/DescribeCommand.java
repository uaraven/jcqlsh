package net.ninjacat.cql.shell;

import com.datastax.driver.core.KeyspaceMetadata;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import org.fusesource.jansi.Ansi;

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
                break;
            case "table":
                break;
            default:
                context.writer().println(ansi().fg(Ansi.Color.RED).a("Improper desc command").reset());
        }
    }

    private static void listKeyspaces(final ShellContext context) {
        final List<KeyspaceMetadata> keyspaces = context.getSession().getCluster().getMetadata().getKeyspaces();
        keyspaces.forEach(keyspaceMetadata -> {
            context.writer().println(keyspaceMetadata.getName());
        });
    }
}
