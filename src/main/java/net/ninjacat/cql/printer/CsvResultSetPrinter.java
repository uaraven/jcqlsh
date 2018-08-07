package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableList;
import net.ninjacat.cql.ShellContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * ResultSetPrinter suitable for exporting data to files. Wraps each cell value in quotes, writes cells separated with
 * commas. Doesn't care about column widths
 */
public class CsvResultSetPrinter extends ResultSetPrinter {

    CsvResultSetPrinter(final ShellContext context) {
        super(context);
    }

    @Override
    void printHeader(final ResultSet resultSet, final List<Integer> columnWidths) {
        getContext().writer().println(StreamSupport.stream(resultSet.getColumnDefinitions().spliterator(), false)
                .map(definition -> escapeQuotes(escapeText(definition.getName())))
                .collect(Collectors.joining(",")));
    }

    @Override
    protected void printRow(final Row row, final List<Integer> columnWidths) {
        final ImmutableList.Builder<String> lineBuilder = ImmutableList.builder();
        for (int i = 0; i < row.getColumnDefinitions().size(); i++) {
            lineBuilder.add("\"" + escapeQuotes(escapeText(safeGetValue(row, i))) + "\"");
        }
        getContext().writer().println(lineBuilder.build().stream().collect(Collectors.joining(",")));
    }

    @Override
    protected List<Integer> calculateColumnWidths(final ColumnDefinitions columns, final List<Row> rows) {
        return ImmutableList.of();
    }

    private static String escapeQuotes(final String line) {
        return line.replaceAll("\"", "\\\"");
    }
}
