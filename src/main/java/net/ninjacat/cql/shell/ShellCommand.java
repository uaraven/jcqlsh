package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;

import java.util.List;

/**
 * Base interface for a shell command.
 */
@FunctionalInterface
public interface ShellCommand {

    void execute(final ShellContext context, final List<Token> tokens);
}
