package net.ninjacat.cql.shell;

import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableMap;
import net.ninjacat.cql.parser.Token;
import org.jline.terminal.Terminal;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Executes shell commands
 */
public class ShellExecutor {
    private final Map<String, Consumer<List<Token>>> COMMANDS = ImmutableMap.<String, Consumer<List<Token>>>builder()
            .put("exit", this::exit)
            .build();

    public ShellExecutor(Session session, Terminal terminal) {
    }

    public boolean isShellCommand(final String command) {
        return COMMANDS.containsKey(command.toLowerCase());
    }

    public void execute(List<Token> tokens) {
        COMMANDS.get(tokens.get(0).getToken().toLowerCase()).accept(tokens.subList(1, tokens.size()));
    }

    private void exit(final List<Token> command) {
        System.exit(0);
    }
}
