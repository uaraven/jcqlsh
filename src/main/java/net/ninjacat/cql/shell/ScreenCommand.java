package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;
import net.ninjacat.cql.printer.ResultSetPrinterType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.ninjacat.cql.utils.Utils.slice;
import static org.fusesource.jansi.Ansi.ansi;

public class ScreenCommand implements ShellCommand {

    private static final String USAGE_ERROR = "Parameter expected. Usage: screen mode|show|paging";

    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> commands = Tokens.stripWhitespace(tokens);
        try {
            if (commands.size() < 2) {
                throw new ShellException(USAGE_ERROR);
            }
            switch (commands.get(1).getToken().toLowerCase()) {
                case "mode":
                    switchMode(context, slice(commands).from(2).tillEnd());
                    break;
                case "show":
                    showTerminalParameters(context);
                    break;
                case "paging":
                    paging(context, slice(commands).from(2).tillEnd());
                    break;
                default:
                    throw new ShellException(USAGE_ERROR);
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }

    private static void showTerminalParameters(final ShellContext context) {
        context.writer().println(ansi().a("Terminal type: ").fgBlue().a(context.getTerminal().getType()).reset());
        context.writer().println(ansi().a("         Name: ").fgBlue().a(context.getTerminal().getName()).reset());
        context.writer().println(ansi().a("        Width: ").fgBlue().a(context.getTerminal().getWidth()).reset());
        context.writer().println(ansi().a("       Height: ").fgBlue().a(context.getTerminal().getHeight()).reset());
        context.writer().println("--------");
        context.writer().println(ansi().a("CQL Formatter: ").fgBlue().a(context.getResultSetPrinter().name()).reset());
    }

    private void switchMode(final ShellContext context, final List<Token> tokens) {
        if (tokens.isEmpty()) {
            context.writer().println(ansi().a("CQL Formatter: ").fgBlue().a(context.getResultSetPrinter().name()).reset());
            return;
        }
        try {
            context.setResultSetPrinter(ResultSetPrinterType.valueOf(tokens.get(0).getToken().toUpperCase()));
        } catch (final IllegalArgumentException ex) {
            context.writer().println(formatError("Invalid mode: ", tokens.get(0).getToken()));
            context.writer().println("Supported modes: " + Arrays.stream(ResultSetPrinterType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")));
        }

    }

    private void paging(final ShellContext context, final List<Token> tokens) {
        if (tokens.isEmpty()) {
            context.writer().println(ansi().a("Paging size: ").fgBlue().a(context.getPaging()).reset());
            return;
        }
        try {
            context.setPaging(Integer.parseInt(tokens.get(0).getToken()));
            context.writer().println(ansi().a("Set paging size to ").fgBlue().a(context.getPaging()).reset());
        } catch (final NumberFormatException ex) {
            context.writer().println(formatError("Invalid page size: ", tokens.get(0).getToken()));
        }
    }
}

