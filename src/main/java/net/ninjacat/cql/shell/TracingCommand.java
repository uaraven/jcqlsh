package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class TracingCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            if (filteredTokens.size() == 2) {
                final String tracing = filteredTokens.get(1).getToken().toUpperCase();
                switch (tracing) {
                    case "ON":
                        if (context.isTracingEnabled()) {
                            context.writer().println(ansi().fgRed().a("Tracing is already enabled. Use TRACING OFF to disable").reset());
                        } else {
                            context.setTracingEnabled(true);
                            context.writer().println("Tracing is enabled");
                        }
                        break;
                    case "OFF":
                        if (!context.isTracingEnabled()) {
                            context.writer().println(ansi().fgRed().a("Tracing is not enabled. Use TRACING ON to enable").reset());
                        } else {
                            context.setTracingEnabled(false);
                            context.writer().println("Disabled tracing");
                        }
                        break;
                    default:
                        throw new ShellException("ON or OFF expected");
                }
            } else {
                context.writer().println(ansi().a("Tracing is currently ").bold().a(context.isTracingEnabled() ? "enabled" : "disabled").reset());
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }
}
