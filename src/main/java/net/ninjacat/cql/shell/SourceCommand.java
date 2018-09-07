package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.cassandra.CqlFileExecutor;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SourceCommand implements ShellCommand {

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> commands = Tokens.stripWhitespace(tokens);
        try {
            if (commands.size() < 2) {
                throw new ShellException("Expected source file name");
            } else {
                final CqlFileExecutor executor = new CqlFileExecutor(context);
                final Path source = Paths.get(commands.get(1).getToken());
                executor.execute(source);
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }
}
