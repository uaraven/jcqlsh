package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

public class CqlResultSetPrinter {


    private final Set<DataType> FLEXIBLE_COLUMN_TYPES = ImmutableSet.of(
            DataType.ascii(),
            DataType.text(),
            DataType.varchar()
    );

    private final ShellContext context;

    public CqlResultSetPrinter(final ShellContext context) {
        this.context = context;
    }


    public void printResultSet(final ResultSet resultSet) {
        final List<Integer> columnWidths = calculateColumnWidths(resultSet);
        this.context.writer().println();
        printHeader(resultSet, columnWidths);
        resultSet.forEach(row -> printRow(row, columnWidths));
        this.context.writer().println();
    }

    /**
     * Prints table header
     *
     * @param resultSet    Result of the query
     * @param columnWidths List of column widths
     */
    private void printHeader(final ResultSet resultSet, final List<Integer> columnWidths) {

        final List<ColumnAndWidth> columnsAndWidths = Streams.zip(
                resultSet.getColumnDefinitions().asList().stream().map(ColumnDefinitions.Definition::getName),
                columnWidths.stream(), ColumnAndWidth::new)
                .collect(Collectors.toList());

        final Ansi ln = ansi();
        final Ansi ln2 = ansi().fgYellow();
        for (int index = 0; index < columnsAndWidths.size(); index++) {
            final ColumnAndWidth cw = columnsAndWidths.get(index);
            if (index == 0) {
                ln.fgYellow().a("| ");
            } else {
                ln.fgYellow().a(" | ");
            }
            ln2.a("+");
            ln.fgBrightBlue().a(StringUtils.center(cw.text, cw.width));
            ln2.a(StringUtils.center("", cw.width + 2, "-"));
            if (index == columnsAndWidths.size() - 1) {
                ln.fgYellow().a(" |");
                ln2.a("+");
            }

        }
        ln.reset();
        ln2.reset();
        this.context.writer().println(ln);
        this.context.writer().println(ln2);
    }

    /**
     * Prints result row
     *
     * @param row          {@link Row} of the result set
     * @param columnWidths List of column widths
     */
    private void printRow(final Row row, final List<Integer> columnWidths) {

        final Ansi ln = ansi();
        for (int index = 0; index < columnWidths.size(); index++) {
            if (index == 0) {
                ln.fgYellow().a("| ");
            } else {
                ln.fgYellow().a(" | ");
            }
            final String text = StringUtils.truncate(row.getObject(index).toString(), columnWidths.get(index) - 1);
            ln.reset().a(StringUtils.center(text, columnWidths.get(index)));
            if (index == columnWidths.size() - 1) {
                ln.fgYellow().a(" |");
            }
        }
        ln.reset();
        this.context.writer().println(ln);
    }

    private int allocateWidthByType(final DataType type) {
        if (this.FLEXIBLE_COLUMN_TYPES.contains(type)) {
            return 40;
        } else if (DataType.uuid().equals(type)) {
            return 36;
        } else {
            return 15;
        }
    }

    private List<Integer> calculateColumnWidths(final ResultSet resultSet) {
        final AtomicInteger totalWidth = new AtomicInteger(this.context.getTerminal().getWidth());

        final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
        return columnDefinitions.asList().stream()
                .map(def -> {
                    int width = allocateWidthByType(def.getType());
                    totalWidth.addAndGet(-width);
                    return width;
                }).collect(Collectors.toList());
    }


    private static final class ColumnAndWidth {
        private final int width;
        private final String text;

        ColumnAndWidth(final String text, final int width) {
            this.width = width;
            this.text = text;
        }
    }
}
