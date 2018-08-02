package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

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
            }
            ln.fgBrightBlue().a(StringUtils.center(cw.text, cw.width));
            ln2.a(StringUtils.center("", cw.width + 1, "-"));
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

    private static int allocateWidthByType(final DataType type, final String columnName) {
        if (FLEXIBLE_COLUMN_TYPES.contains(type)) {
            return DEFAULT_WIDTH;
        } else if (DataType.uuid().equals(type)) {
            return 36;
        } else {
            return Math.max(16, columnName.length());
        }
    }

    @Override
    protected List<Integer> calculateColumnWidths(final ResultSet resultSet, List<Row> rows) {
        final AtomicInteger totalWidth = new AtomicInteger(getContext().getTerminal().getWidth() + 1);

        final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();

        final List<Integer> result = IntStream.range(0, columnDefinitions.size()).mapToObj(it -> 0).collect(Collectors.toList());

        // first allocate all fixed columns
        Streams.zip(
                IntStream.range(0, columnDefinitions.size()).boxed(),
                columnDefinitions.asList().stream(),
                IndexedColumnDef::new)
                .filter(icd -> !FLEXIBLE_COLUMN_TYPES.contains(icd.definition.getType()))
                .forEach(icd -> {
                    final int width = allocateWidthByType(icd.definition.getType(), icd.definition.getName());
                    totalWidth.addAndGet(-width - 3);
                    result.set(icd.index, width);
                });

        // then split rest of available screen width between flex columns
        final long flexWidthColumns = columnDefinitions.asList().stream().filter(def -> FLEXIBLE_COLUMN_TYPES.contains(def.getType())).count();
        if (totalWidth.get() > DEFAULT_WIDTH * flexWidthColumns) {
            final int flexColumnWidth = (int) ((totalWidth.get() - flexWidthColumns * 3) / flexWidthColumns);

            // and fill in spaces
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i) == 0) {
                    result.set(i, flexColumnWidth);
                }
            }

        } else {
            // and fill in spaces
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i) == 0) {
                    result.set(i, DEFAULT_WIDTH);
                }
            }
        }
        return result;
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

    private static final class IndexedColumnDef {
        private final int index;
        private final ColumnDefinitions.Definition definition;

        private IndexedColumnDef(final int index, final ColumnDefinitions.Definition definition) {
            this.index = index;
            this.definition = definition;
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
