package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.printer.CqlResultPrinter;
import net.ninjacat.cql.printer.ResultSetPrinterProvider;
import net.ninjacat.cql.utils.Exceptions;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Executes Cql queries
 */
public class CqlExecutor {


    private final ShellContext context;
    private final ResultSetPrinterProvider printerProvider;

    public CqlExecutor(final ShellContext context) {
        this.context = context;
        this.printerProvider = new ResultSetPrinterProvider(context);
    }


    /**
     * Executes a query. Prints result in a table
     *
     * @param line List of tokens comprising a query string. Must contain all the original whitespace
     */
    public void execute(final List<Token> line) {
        final String cqlQuery = buildQueryLine(line);
        execute(cqlQuery);
    }

    public void execute(final String cqlQuery) {
        execute(cqlQuery, this.context.isTracingEnabled());
    }

    public void execute(final String cqlQuery, final boolean tracing) {
        try {
            final PreparedStatement preparedStatement = this.context.getSession().prepare(cqlQuery)
                    .setConsistencyLevel(this.context.getConsistencyLevel())
                    .setSerialConsistencyLevel(this.context.getSerialConsistencyLevel());
            if (tracing) {
                preparedStatement.enableTracing();
            }
            final ResultSet resultSet = this.context.getSession().execute(preparedStatement.bind());
            getResultSetPrinter().printResultSet(resultSet);
        } catch (final Exception ex) {
            this.context.writer().println(Exceptions.toAnsiException(ex));
        }
    }

    private CqlResultPrinter getResultSetPrinter() {
        return this.printerProvider.get(this.context.getResultSetPrinter());
    }


    private static String buildQueryLine(final List<Token> line) {
        return line.stream().map(Token::getToken).collect(Collectors.joining(""));
    }

}
