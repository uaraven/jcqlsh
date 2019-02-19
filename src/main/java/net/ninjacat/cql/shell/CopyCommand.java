package net.ninjacat.cql.shell;

import com.google.common.base.Joiner;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;

import java.util.List;

public class CopyCommand implements ShellCommand {
    @Override
    public void execute(ShellContext context, List<Token> tokens) {
        final String copyCommand = Joiner.on(" ").join(tokens);

    }
}
