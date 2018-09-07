package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * ResultSetPrinter that sizes all columns to fit in terminal.
 * Long lines are wrapped inside column, so each row may span multiple terminal lines.
 * <p>
 * This view is easy on the eyes, but hard to copy data
 */
@SuppressWarnings("UnstableApiUsage")
public class NiceResultSetPrinter extends ResultSetPrinter implements ScalableColumnsWidthCalculator {


    public NiceResultSetPrinter(final ShellContext context) {
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
        final Cells cells = buildRowCells(row, columnWidths);
        cells.print(getContext().writer());
    }

    private Cells buildRowCells(final Row row, final List<Integer> columnWidths) {
        return new Cells(IntStream.range(0, columnWidths.size())
                .mapToObj(index -> new Cell(escapeText(Objects.toString(row.getObject(index), "<null>")), columnWidths.get(index)))
                .collect(Collectors.toList()));
    }

    @Override
    protected List<Integer> calculateColumnWidths(final ColumnDefinitions columns, final List<Row> rows) {
        return columnWidths(getContext(), columns, rows);
    }

    @Override
    public String getText(final Row row, final int idx) {
        return escapeText(safeGetValue(row, idx));
    }

    private final class Cells {
        final List<Cell> cells;
        private final int totalLines;
        int currentLine;

        private Cells(final List<Cell> cells) {
            this.cells = cells;
            this.totalLines = cells.stream().mapToInt(cell -> cell.lines.size()).max().orElse(0);
            this.currentLine = 0;
        }

        private List<ColumnAndWidth> nextLine() {
            if (this.currentLine < this.totalLines) {
                final List<ColumnAndWidth> line = this.cells.stream()
                        .map(cell -> new ColumnAndWidth(cell.getLine(this.currentLine), cell.cellWidth))
                        .collect(Collectors.toList());
                this.currentLine += 1;
                return line;
            } else {
                return ImmutableList.of();
            }
        }

        private void print(final PrintWriter writer) {
            List<ColumnAndWidth> line = nextLine();

            while (!line.isEmpty()) {
                final Ansi ln = ansi();

                for (int index = 0; index < line.size(); index++) {
                    if (index > 0) {
                        separator(ln).a(" | ");
                    } else {
                        ln.a(" ");
                    }
                    final String text = Strings.nullToEmpty(line.get(index).text);
                    ln.reset().a(StringUtils.rightPad(text, line.get(index).width));
                }
                ln.reset();
                writer.println(ln);
                line = nextLine();
            }
        }
    }


    private static final class Cell {
        final List<String> lines;
        private final int cellWidth;

        private Cell(final String line, final int cellWidth) {
            this.cellWidth = cellWidth;
            this.lines = ImmutableList.copyOf(line.split("(?<=\\G.{" + cellWidth + "})"));
        }

        String getLine(final int index) {
            if (index >= this.lines.size()) {
                return null;
            } else {
                return this.lines.get(index);
            }
        }

    }

}
