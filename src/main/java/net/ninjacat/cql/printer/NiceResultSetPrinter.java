package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

@SuppressWarnings("UnstableApiUsage")
public class NiceResultSetPrinter extends BaseResultSetPrinter {


    public NiceResultSetPrinter(final ShellContext context) {
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
            if (index > 0) {
                ln.fgYellow().a(" | ");
                ln2.a("+");
            } else {
                ln.a(" ");
            }
            ln.fgBrightBlue().a(StringUtils.center(cw.text, cw.width));
            ln2.a(StringUtils.center("", cw.width + (index == columnsAndWidths.size() - 1 ? 1 : 2), "-"));
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
        final Cells cells = buildRowCells(row, columnWidths);
        cells.print(getContext().writer());
    }

    private Cells buildRowCells(final Row row, final List<Integer> columnWidths) {
        return new Cells(IntStream.range(0, columnWidths.size())
                .mapToObj(index -> new Cell(escapeText(Objects.toString(row.getObject(index), "<null>")), columnWidths.get(index)))
                .collect(Collectors.toList()));
    }

    @Override
    protected List<Integer> calculateColumnWidths(final ResultSet resultSet, final List<Row> rows) {
        final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();

        final int separatorOverhead = (columnDefinitions.size() - 1) * 3 - 1;

        final IntStream defaultColumnWidths = columnDefinitions.asList().stream().mapToInt(def -> def.getName().length());

        final int totalWidth = getContext().getTerminal().getWidth() - separatorOverhead;

        final List<Integer> columnWidths = rows.stream().map(
                row -> IntStream.range(0, columnDefinitions.size())
                        .map(idx -> escapeText(safeGetValue(row, idx)).length()))
                .reduce(defaultColumnWidths,
                        (is1, is2) -> Streams.zip(is1.boxed(), is2.boxed(), Math::max).mapToInt(Integer::intValue))
                .boxed()
                .collect(Collectors.toList());

        final int totalColumnWidths = columnWidths.stream().mapToInt(i -> i).sum();

        if (totalColumnWidths < totalWidth) {
            return columnWidths;
        } else {
            final List<Integer> result = IntStream.range(0, columnDefinitions.size()).mapToObj(it -> 0).collect(Collectors.toList());

            final List<IndexedWidth> sortedFlexCols = IntStream.range(0, columnWidths.size())
                    .mapToObj(idx -> new IndexedWidth(idx, columnWidths.get(idx), FLEX_TYPES.contains(columnDefinitions.getType(idx))))
                    .sorted((iw1, iw2) -> {
                        int c = Boolean.compare(iw1.flexible, iw2.flexible);
                        if (c == 0) {
                            return Integer.compare(iw1.width, iw2.width);
                        } else {
                            return c;
                        }
                    })
                    .collect(Collectors.toList());

            final int fixedColumnWs = sortedFlexCols.stream().filter(iw -> !iw.flexible).mapToInt(iw -> iw.width).sum();
            final int flexColumnWs = totalWidth - fixedColumnWs;
            int widthLeft = totalWidth - fixedColumnWs;

            final int threshold = (int) (widthLeft / sortedFlexCols.stream().filter(iw -> iw.flexible).count());
            for (final IndexedWidth iw : sortedFlexCols) {
                if (!iw.flexible || iw.width < threshold) {
                    result.set(iw.index, iw.width);
                    if (iw.flexible) {
                        widthLeft -= iw.width;
                    }
                } else {
                    final int newW = iw.width * widthLeft / flexColumnWs - 3;
                    result.set(iw.index, newW);
                }
            }

            return result;
        }
    }

    private static final class Cells {
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
                        ln.fgYellow().a(" | ");
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

    private static final class IndexedWidth {
        private final int index;
        private final int width;
        private final boolean flexible;

        private IndexedWidth(final int index, final int width, final boolean flexible) {
            this.index = index;
            this.width = width;
            this.flexible = flexible;
        }
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
