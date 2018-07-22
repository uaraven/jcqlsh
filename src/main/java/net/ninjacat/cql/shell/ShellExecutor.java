package net.ninjacat.cql.shell;

import com.google.common.collect.ImmutableMap;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;

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
            .build();


    private ShellContext context;

    public ShellExecutor(final ShellContext context) {
        this.context = context;
    }

    public boolean isShellCommand(final String command) {
        return COMMANDS.containsKey(command.toLowerCase());
    }

    public void execute(final List<Token> tokens) {
        COMMANDS.get(tokens.get(0).getToken().toLowerCase()).execute(context, tokens.subList(1, tokens.size()));
    }

    private static void exit(final ShellContext context, final List<Token> command) {
        System.exit(0);
    }
}
