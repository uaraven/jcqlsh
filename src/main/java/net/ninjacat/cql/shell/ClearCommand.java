package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

public class ClearCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        if (ShellContext.isRunningInTerminal()) {
            ansi().eraseScreen(Ansi.Erase.ALL);
        } else {
            IntStream.range(0, context.getScreenSettings().getPaging() * 2).forEach(i -> context.writer().println());
        }
    }
}
