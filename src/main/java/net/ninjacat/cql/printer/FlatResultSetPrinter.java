package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * ResultSetPrinter which prints every row in one line. Column widths are calculated to accommodate longest cell in
 * currently fetched rows.
 * <p>
 * This behaves more or less the same as original {@code cqlsh}
 */
@SuppressWarnings("UnstableApiUsage")
public class FlatResultSetPrinter extends ResultSetPrinter {


    FlatResultSetPrinter(final ShellContext context) {
        super(context);
    }

    /**
     * Prints result row
     *
     * @param row          {@link Row} of the result set
     * @param columnWidths List of column widths
     */
    @Override
    protected void printRow(final Row row, final List<Integer> columnWidths) {
        final Ansi ln = ansi();
        for (int index = 0; index < row.getColumnDefinitions().size(); index++) {
            if (index > 0) {
                ln.fgYellow().a(" | ");
            } else {
                ln.a(" ");
            }
            final Object value = row.getObject(index);
            final String text = value == null ? "<null>" : escapeText(value.toString());
            ln.reset().a(StringUtils.leftPad(text, columnWidths.get(index)));
            ln.reset();
        }
        this.getContext().writer().println(ln);
    }


    @Override
    protected List<Integer> calculateColumnWidths(final ColumnDefinitions columns, final List<Row> rows) {
        final IntStream defaultColumnWidths = columns.asList().stream().mapToInt(def -> def.getName().length());

        // first allocate all fixed columns
        return rows.stream().map(
                row -> IntStream.range(0, columns.size())
                        .map(idx -> escapeText(safeGetValue(row, idx)).length()))
                .reduce(defaultColumnWidths,
                        (is1, is2) -> Streams.zip(is1.boxed(), is2.boxed(), Math::max).mapToInt(Integer::intValue))
                .boxed().collect(Collectors.toList());
    }

}
