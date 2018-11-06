package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.Iterators;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Prints each row vertically
 */
public class ExpandedResultSetPrinter implements CqlResultPrinter {

    private final AtomicInteger rowCounter;
    private final ShellContext context;

    ExpandedResultSetPrinter(final ShellContext context) {
        this.context = context;
        this.rowCounter = new AtomicInteger(0);
    }

    @Override
    public void printResultSet(final ResultSet resultSet) {
        if (!resultSet.getColumnDefinitions().asList().isEmpty()) {

            final Iterator<List<Row>> resultPages = Iterators.partition(resultSet.iterator(), this.context.getScreenSettings().getPaging());
            while (resultPages.hasNext()) {
                this.context.writer().println();
                final List<Row> results = resultPages.next();
                results.forEach(this::printRow);
                if (ShellContext.isRunningInTerminal() && resultPages.hasNext()) {
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

    private void printRow(final Row row) {
        final ResultSetColorizer color = this.context.getResultColorizer();
        this.context.writer().println(String.format("@ Row %d", this.rowCounter.incrementAndGet()));
        this.context.writer().println(color.table(ansi()).a(StringUtils.center("", this.context.getTerminal().getWidth(), "-")).reset());
        final int columnWidth = row.getColumnDefinitions().asList().stream().mapToInt(it -> it.getName().length()).max().orElse(20);
        int index = 0;
        for (final ColumnDefinitions.Definition column : row.getColumnDefinitions().asList()) {
            final Ansi ln = ansi().a(" ");
            color.header(ln, column).a(StringUtils.leftPad(column.getName(), columnWidth)).reset();
            color.table(ln).a(" | ");
            final String text = escapeText(safeGetValue(row, index));
            color.value(ln, column).a(text);
            this.context.writer().println(ln);
            index += 1;
        }
    }
}
