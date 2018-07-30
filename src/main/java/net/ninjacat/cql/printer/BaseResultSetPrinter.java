package net.ninjacat.cql.printer;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import net.ninjacat.cql.ShellContext;

import java.util.List;
import java.util.Set;

public abstract class BaseResultSetPrinter implements ResultSetPrinter {

    protected static final Set<DataType> FLEXIBLE_COLUMN_TYPES = ImmutableSet.of(
            DataType.ascii(),
            DataType.text(),
            DataType.varchar(),
            DataType.blob()
    );
    protected static final int DEFAULT_WIDTH = 40;

    private final ShellContext context;

    public BaseResultSetPrinter(final ShellContext context) {
        this.context = context;
    }

    @Override
    public void printResultSet(final ResultSet resultSet) {
        if (!resultSet.getColumnDefinitions().asList().isEmpty()) {
            final List<Integer> columnWidths = calculateColumnWidths(resultSet);
            this.context.writer().println();
            printHeader(resultSet, columnWidths);
            resultSet.forEach(row -> printRow(row, columnWidths));
            this.context.writer().println();
        }
    }

    public ShellContext getContext() {
        return this.context;
    }

    protected abstract void printRow(Row row, List<Integer> columnWidths);

    protected abstract void printHeader(ResultSet resultSet, List<Integer> columnWidths);

    protected abstract List<Integer> calculateColumnWidths(ResultSet resultSet);

}
