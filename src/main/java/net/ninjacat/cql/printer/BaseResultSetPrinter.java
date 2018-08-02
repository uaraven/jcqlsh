package net.ninjacat.cql.printer;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import net.ninjacat.cql.ShellContext;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class BaseResultSetPrinter implements ResultSetPrinter {

    static final Set<DataType> FLEXIBLE_COLUMN_TYPES = ImmutableSet.of(
            DataType.ascii(),
            DataType.text(),
            DataType.varchar(),
            DataType.blob()
    );
    static final int DEFAULT_WIDTH = 40;

    private final ShellContext context;

    BaseResultSetPrinter(final ShellContext context) {
        this.context = context;
    }

    @Override
    public void printResultSet(final ResultSet resultSet) {
        if (!resultSet.getColumnDefinitions().asList().isEmpty()) {

            final int pageSize = Math.max(this.context.getTerminal().getHeight(), 50);

            final Iterator<List<Row>> resultPages = Iterators.partition(resultSet.iterator(), pageSize);
            while (resultPages.hasNext()) {
                this.context.writer().println();
                final List<Row> results = resultPages.next();
                final List<Integer> columnWidths = calculateColumnWidths(resultSet, results);
                printHeader(resultSet, columnWidths);
                results.forEach(row -> printRow(row, columnWidths));
                if (resultPages.hasNext()) {
                    this.context.writer().print("-- MORE --");
                    this.context.writer().flush();
                    this.context.waitForKeypress();
                }
                this.context.writer().println();
            }
        }
    }

    public ShellContext getContext() {
        return this.context;
    }

    public String escapeText(final String text) {
        return text.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    protected abstract void printRow(Row row, List<Integer> columnWidths);

    protected abstract void printHeader(ResultSet resultSet, List<Integer> columnWidths);

    protected abstract List<Integer> calculateColumnWidths(ResultSet resultSet, List<Row> rows);

}
