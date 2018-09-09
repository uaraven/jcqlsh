package net.ninjacat.cql.shell;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.parser.Tokens;
import net.ninjacat.cql.printer.ResultSetPrinterType;

import javax.sql.rowset.serial.SerialException;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class ExpandCommand implements ShellCommand {
    @Override
    public void execute(final ShellContext context, final List<Token> tokens) {
        final List<Token> filteredTokens = Tokens.stripWhitespace(tokens);
        try {
            if (filteredTokens.size() == 2) {
                final String mode = filteredTokens.get(1).getToken().toUpperCase();
                try {
                    if ("ON".equals(mode)) {
                        if (context.getResultSetPrinter() == ResultSetPrinterType.EXPANDED) {
                            context.writer().println(ansi().fgRed().a("Expanded output is already enabled. Use EXPAND OFF to disable"));
                        } else {
                            context.setResultSetPrinter(ResultSetPrinterType.EXPANDED);
                            context.writer().println(ansi().a("Expanded output is now ").bold().a("enabled").reset());
                        }
                    } else if ("OFF".equals(mode)) {
                        if (context.getResultSetPrinter() != ResultSetPrinterType.EXPANDED) {
                            context.writer().println(ansi().fgRed().a("Expanded output is already disabled. Use EXPAND ON to disable"));
                        } else {
                            context.setResultSetPrinter(ResultSetPrinterType.COMPACT);
                            context.writer().println(ansi().a("Expanded output is now ").bold().a("disabled").reset());
                        }
                    } else {
                        throw new SerialException("ON or OFF expected");
                    }
                } catch (final Exception ex) {
                    throw new ShellException("Invalid consistency: " + mode);
                }
            } else {
                context.writer().print(ansi().a("Expanded output is currently "));
                if (context.getResultSetPrinter() == ResultSetPrinterType.EXPANDED) {
                    context.writer().println(ansi().bold().a("enabled").reset().a("Use EXPAND OFF to enable"));
                } else {
                    context.writer().println(ansi().bold().a("disabled").reset().a("Use EXPAND ON to enable"));
                }
            }
        } catch (final ShellException ex) {
            context.writer().println(formatError("Improper %s command: %s", tokens.get(0), ex.getMessage()));
        }
    }
}
