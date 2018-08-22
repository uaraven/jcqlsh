package net.ninjacat.cql.shell;

import com.datastax.driver.core.ConsistencyLevel;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class SerialConsistencyCommand implements ShellCommand {

    private static final String CONSISTENCY_EXPECTED = "'consistency' expected";

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            if (filteredTokens.size() < 2) {
                throw new ShellException(CONSISTENCY_EXPECTED);
            }
            if (!"consistency".equalsIgnoreCase(filteredTokens.get(1).getToken())) {
                throw new ShellException(CONSISTENCY_EXPECTED);
            }
            if (filteredTokens.size() == 3) {
                final String consistency = filteredTokens.get(2).getToken().toUpperCase();
                final ConsistencyLevel level;
                try {
                    level = ConsistencyLevel.valueOf(consistency);
                    if (level != ConsistencyLevel.SERIAL && level != ConsistencyLevel.LOCAL_SERIAL) {
                        throw new ShellException("SERIAL and LOCAL_SERIAL levels only");
                    }
                    context.setSerialConsistencyLevel(level);
                    context.writer().println(ansi().a("Serial consistency level set to ").bold().a(context.getConsistencyLevel().name()).reset());
                } catch (final Exception ex) {
                    throw new ShellException("Invalid consistency: " + consistency);
                }
            } else {
                context.writer().println(ansi().a("Current serial consistency level is ").bold().a(context.getSerialConsistencyLevel().name()).reset());
            }

        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }
}
