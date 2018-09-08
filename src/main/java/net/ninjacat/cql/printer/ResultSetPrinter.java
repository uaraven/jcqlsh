package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Base class for ResultSetPrinters.
 * <p>
 * Incapsulates layout strategy to print rows in {@link ResultSet} on screen.
 */
public abstract class ResultSetPrinter implements CqlResultPrinter {

    private final ShellContext context;

    ResultSetPrinter(final ShellContext context) {
        this.context = context;
    }

    /**
     * Prints table header
     *
     * @param resultSet    Result of the query
     * @param columnWidths List of column widths
     */
    void printHeader(final ResultSet resultSet, final List<Integer> columnWidths) {

        final List<ColumnAndWidth> columnsAndWidths = Streams.zip(
                resultSet.getColumnDefinitions().asList().stream().map(ColumnDefinitions.Definition::getName),
                columnWidths.stream(), ColumnAndWidth::new)
                .collect(Collectors.toList());

        final Ansi ln = ansi();
        final Ansi ln2 = separator(ansi());
        for (int index = 0; index < columnsAndWidths.size(); index++) {
            final ColumnAndWidth cw = columnsAndWidths.get(index);
            if (index > 0) {
                separator(ln).a(" | ");
                ln2.a("+");
            } else {
                ln.a(" ");
            }
            header(ln).a(StringUtils.center(cw.text, cw.width));
            ln2.a(StringUtils.center("", cw.width + (index == columnsAndWidths.size() - 1 ? 1 : 2), "-"));
        }
        ln.reset();
        ln2.reset();
        getContext().writer().println(ln);
        getContext().writer().println(ln2);
    }

    /**
     * Prints result set with paging.
     *
     * @param resultSet {@link ResultSet} containing data.
     */
    @Override
    public void printResultSet(final ResultSet resultSet) {
        if (!resultSet.getColumnDefinitions().asList().isEmpty()) {

            final Iterator<List<Row>> resultPages = Iterators.partition(resultSet.iterator(), this.context.getScreenSettings().getPaging());
            while (resultPages.hasNext()) {
                this.context.writer().println();
                final List<Row> results = resultPages.next();
                final List<Integer> columnWidths = calculateColumnWidths(resultSet.getColumnDefinitions(), results);
                printHeader(resultSet, columnWidths);
                results.forEach(row -> printRow(row, columnWidths));
                if (this.context.isRunningInTerminal() && resultPages.hasNext()) {
                    this.context.writer().print("-- ENTER for MORE --");
                    this.context.writer().flush();
                    if (!this.context.waitForKeypress()) {
                        break;
                    }
                }
                this.context.writer().println();
            }
        }
    }

    public ShellContext getContext() {
        return this.context;
    }

    /**
     * Prints one row of result set.
     * @param row Result set row
     * @param columnWidths Widths of columns
     */
    protected abstract void printRow(Row row, List<Integer> columnWidths);

    /**
     * Calculates column widths for output.
     * <p>
     * Usual implementations will calculate widths using terminal width, columns definitions and data in the fetched rows.
     * It is ok to have different column widths for each set of fetched rows (that's how original CQLSH works)
     *
     * @param columns {@link ColumnDefinitions} for the result set
     * @param rows    List of fetched rows
     * @return List of column widths
     */
    protected abstract List<Integer> calculateColumnWidths(ColumnDefinitions columns, List<Row> rows);

    static final class ColumnAndWidth {
        final int width;
        final String text;

        ColumnAndWidth(final String text, final int width) {
            this.width = width;
            this.text = text;
        }
    }


}
