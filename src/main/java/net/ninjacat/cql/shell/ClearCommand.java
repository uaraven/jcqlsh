package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;

import java.util.List;
import java.util.stream.IntStream;

public class ClearCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        IntStream.range(0, Math.max(context.getTerminal().getHeight(), 50)).forEach($ -> context.writer().println());
    }
}
