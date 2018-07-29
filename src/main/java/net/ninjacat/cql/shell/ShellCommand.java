package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import org.fusesource.jansi.Ansi;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Base interface for a shell command.
 */
@FunctionalInterface
public interface ShellCommand {

    void execute(final ShellContext context, final List<Token> tokens);

    default Ansi formatError(final String message, final Object... params) {
        return ansi().fgRed().a(String.format(message, params)).reset();
    }
}
