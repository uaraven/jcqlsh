package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * ResultSet printer which dynamically calculates column widths to fit all columns into terminal width.
 * <p>
 * Will trim long lines so that each row can be printed on one line
 */
@SuppressWarnings("UnstableApiUsage")
public class CompactResultSetPrinter extends ResultSetPrinter implements ScalableColumnsWidthCalculator {

    CompactResultSetPrinter(final ShellContext context) {
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
            if (text.length() > columnWidths.get(index)) {
                ln.reset().a(text.substring(0, columnWidths.get(index) - 1) + "…");
            } else {
                ln.reset().a(StringUtils.leftPad(text, columnWidths.get(index)));
            }
            ln.reset();
        }
        this.getContext().writer().println(ln);
    }

    @Override
    protected List<Integer> calculateColumnWidths(final ColumnDefinitions columns, final List<Row> rows) {
        return columnWidths(getContext(), columns, rows);
    }

    @Override
    public String getText(final Row row, final int idx) {
        return escapeText(safeGetValue(row, idx));
    }

}
