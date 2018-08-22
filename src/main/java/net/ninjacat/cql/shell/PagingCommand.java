package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class PagingCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> commands = Tokens.stripWhitespace(tokens);
        try {
            if (commands.size() < 2) {
                if (context.isPagingEnabled()) {
                    showPagingEnabled(context);
                } else {
                    showPagingDisabled(context);
                }
            } else {
                switch (commands.get(1).getToken().toLowerCase()) {
                    case "on":
                        context.setPagingEnabled(true);
                        showPagingEnabled(context);
                        break;
                    case "off":
                        context.setPagingEnabled(false);
                        showPagingDisabled(context);
                        break;
                    default:
                        try {
                            context.setPaging(Integer.parseInt(commands.get(1).getToken()));
                            context.writer().println(ansi().a("Page size: ").bold().a(context.getPaging()).reset());
                        } catch (final NumberFormatException ex) {
                            throw new ShellException("Invalid parameter. Expected ON|OFF|<PAGE SIZE>");
                        }
                }
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }

    private static void showPagingEnabled(final ShellContext context) {
        context.writer().println(ansi().a("Query paging is currently ").bold().a("enabled").boldOff()
                .a(".\n Page size: ").bold().a(context.getPaging()).reset());
    }

    private static void showPagingDisabled(final ShellContext context) {
        context.writer().println(ansi().a("Paging is currently ").bold()
                .a("disabled").boldOff().a("Use PAGING ON to enable."));
    }
}
