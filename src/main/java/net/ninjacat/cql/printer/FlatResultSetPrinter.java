package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

@SuppressWarnings("UnstableApiUsage")
public class FlatResultSetPrinter extends BaseResultSetPrinter {


    FlatResultSetPrinter(final ShellContext context) {
        super(context);
    }

    /**
     * Prints table header
     *
     * @param resultSet    Result of the query
     * @param columnWidths List of column widths
     */
    @Override
    protected void printHeader(final ResultSet resultSet, final List<Integer> columnWidths) {

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
        getContext().writer().println(ln);
        getContext().writer().println(ln2);
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
            if (index == 0) {
                ln.fgYellow().a("| ");
            } else {
                ln.fgYellow().a(" | ");
            }
            final Object value = row.getObject(index);
            final String text = value == null ? "<null>" : escapeText(value.toString());
            ln.reset().a(StringUtils.leftPad(text, columnWidths.get(index)));
            if (index == row.getColumnDefinitions().size() - 1) {
                ln.fgYellow().a(" |");
            }
            ln.reset();
        }
        this.getContext().writer().println(ln);
    }


    @Override
    protected List<Integer> calculateColumnWidths(final ResultSet resultSet, final List<Row> rows) {
        final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();

        final IntStream defaultColumnWidths = columnDefinitions.asList().stream().mapToInt(def -> def.getName().length());

        // first allocate all fixed columns
        return rows.stream().map(
                row -> IntStream.range(0, columnDefinitions.size())
                        .map(idx -> escapeText(safeGetValue(row, idx)).length()))
                .reduce(defaultColumnWidths,
                        (is1, is2) -> Streams.zip(is1.boxed(), is2.boxed(), Math::max).mapToInt(Integer::intValue))
                .boxed().collect(Collectors.toList());
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
