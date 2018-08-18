package net.ninjacat.cql.shell;

import com.datastax.driver.core.ConsistencyLevel;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class ConsistencyCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            if (filteredTokens.size() == 2) {
                final String consistency = filteredTokens.get(1).getToken().toUpperCase();
                final ConsistencyLevel level;
                try {
                    level = ConsistencyLevel.valueOf(consistency);
                    context.setConsistencyLevel(level);
                    context.writer().println(ansi().a("Consistency level set to ").bold().a(context.getConsistencyLevel().name()).reset());
                } catch (final Exception ex) {
                    throw new ShellException("Invalid consistency: " + consistency);
                }
            } else {
                context.writer().println(ansi().a("Current consistency level is ").bold().a(context.getConsistencyLevel().name()).reset());
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }
}
