package net.ninjacat.cql.shell;

import com.google.common.base.Joiner;
import net.ninjacat.cql.CqlCopyLexer;
import net.ninjacat.cql.CqlCopyParser;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.copy.*;
import net.ninjacat.cql.parser.Token;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class CopyCommand implements ShellCommand {
    @Override
    public void execute(ShellContext context, List<Token> tokens) {
        final String copyCommand = Joiner.on(" ").join(tokens);
        final CqlCopyContext copyContext = parseCopyCommand(copyCommand);

        BaseCopy copyExecutor = copyContext.getDirection() == CopyDirection.FROM ? new CopyFrom(copyContext) : new CopyTo(copyContext);
    }

    private static CqlCopyContext parseCopyCommand(final String text) {
        final CqlCopyLexer cqlCopyLexer = new CqlCopyLexer(CharStreams.fromString(text));
        final CqlCopyParser cqlCopyParser = new CqlCopyParser(new CommonTokenStream(cqlCopyLexer));
        final ParseTreeWalker walker = new ParseTreeWalker();
        final CopyBuilder copyBuilder = new CopyBuilder();
        walker.walk(copyBuilder, cqlCopyParser.copy_stmt());

        return copyBuilder.getCopyData().validate();
    }
}
