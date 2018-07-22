package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.ResultSet;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.parser.Token;
import net.ninjacat.cql.utils.Exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class CqlExecutor {

    private final ShellContext context;

    public CqlExecutor(final ShellContext context) {
        this.context = context;
    }

    public void execute(final List<Token> line) {
        final String cqlQuery = buildQueryLine(line);
        try {
            final ResultSet resultSet = this.context.getSession().execute(cqlQuery);
            this.context.writer().print("Number of rows: ");
            this.context.writer().println(resultSet.getAvailableWithoutFetching());
        } catch (final Exception ex) {
            this.context.writer().println(Exceptions.toAnsiException(ex));
        }
    }

    private static String buildQueryLine(final List<Token> line) {
        return line.stream().map(Token::getToken).collect(Collectors.joining(" "));
    }
}
