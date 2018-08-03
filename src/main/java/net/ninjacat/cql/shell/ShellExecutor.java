package net.ninjacat.cql.shell;

import com.google.common.collect.ImmutableMap;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * Executes shell commands
 */
public class ShellExecutor {
    private static final DescribeCommand DESCRIBE = new DescribeCommand();

    private static final Map<String, ShellCommand> COMMANDS = ImmutableMap.<String, ShellCommand>builder()
            .put("exit", ShellExecutor::exit)
            .put("desc", DESCRIBE)
            .put("describe", DESCRIBE)
            .put("show", new ShowCommand())
            .put("screen", new ScreenCommand())
            .build();


    private final ShellContext context;

    public ShellExecutor(final ShellContext context) {
        this.context = context;
    }

    public static boolean isShellCommand(final String command) {
        return COMMANDS.containsKey(command.toLowerCase());
    }

    private static void exit(final ShellContext context, final List<Token> command) {
        Utils.closeQuietly(context.getTerminal());
        System.exit(0);
    }

    public void execute(final List<Token> tokens) {
        COMMANDS.get(tokens.get(0).getToken().toLowerCase()).execute(this.context, tokens);
    }
}
