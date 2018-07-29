package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;

public class CqlResultSetPrinter {


    private static final Set<DataType> FLEXIBLE_COLUMN_TYPES = ImmutableSet.of(
            DataType.ascii(),
            DataType.text(),
            DataType.varchar()
    );
    public static final int DEFAULT_WIDTH = 40;

    private final ShellContext context;

    public CqlResultSetPrinter(final ShellContext context) {
        this.context = context;
    }


    public void printResultSet(final ResultSet resultSet) {
        if (!resultSet.getColumnDefinitions().asList().isEmpty()) {
            final List<Integer> columnWidths = calculateColumnWidths(resultSet);
            this.context.writer().println();
            printHeader(resultSet, columnWidths);
            resultSet.forEach(row -> printRow(row, columnWidths));
            this.context.writer().println();
        }
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
        final Cells cells = buildRowCells(row, columnWidths);
        cells.print(this.context.writer());
    }

    private Cells buildRowCells(final Row row, final List<Integer> columnWidths) {
        return new Cells(IntStream.range(0, columnWidths.size())
                .mapToObj(index -> new Cell(row.getObject(index).toString(), columnWidths.get(index)))
                .collect(Collectors.toList()));
    }

    private int allocateWidthByType(final DataType type) {
        if (FLEXIBLE_COLUMN_TYPES.contains(type)) {
            return DEFAULT_WIDTH;
        } else if (DataType.uuid().equals(type)) {
            return 36;
        } else {
            return 20;
        }
    }

    private List<Integer> calculateColumnWidths(final ResultSet resultSet) {
        final AtomicInteger totalWidth = new AtomicInteger(this.context.getTerminal().getWidth());

        final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();

        final List<Integer> result = IntStream.range(0, columnDefinitions.size()).mapToObj(it -> 0).collect(Collectors.toList());

        // first allocate all fixed columns
        Streams.zip(
                IntStream.range(0, columnDefinitions.size()).boxed(),
                columnDefinitions.asList().stream(),
                IndexedColumnDef::new)
                .filter(icd -> !FLEXIBLE_COLUMN_TYPES.contains(icd.definition.getType()))
                .forEach(icd -> {
                    final int width = allocateWidthByType(icd.definition.getType());
                    totalWidth.addAndGet(-width);
                    result.set(icd.index, width);
                });

        // then split rest of available screen width between flex columns
        final long flexWidthColumns = columnDefinitions.asList().stream().filter(def -> FLEXIBLE_COLUMN_TYPES.contains(def.getType())).count();
        if (totalWidth.get() > DEFAULT_WIDTH * flexWidthColumns) {
            final int flexColumnWidth = (int) ((totalWidth.get() - flexWidthColumns * 6) / flexWidthColumns);

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

        private Cells(final List<Cell> cells) {
            this.cells = cells;
        }

        private List<ColumnAndWidth> nextLine() {
            final List<ColumnAndWidth> line = this.cells.stream()
                    .map(cell -> new ColumnAndWidth(cell.nextLine(), cell.cellWidth))
                    .collect(Collectors.toList());
            if (line.stream().allMatch(cell -> cell.text == null)) {
                return ImmutableList.of();
            } else {
                return line;
            }
        }

        private void print(final PrintWriter writer) {
            List<ColumnAndWidth> line = nextLine();
            final Ansi ln = ansi();

            while (!line.isEmpty()) {
                for (int index = 0; index < line.size(); index++) {
                    if (index == 0) {
                        ln.fgYellow().a("| ");
                    } else {
                        ln.fgYellow().a(" | ");
                    }
                    final String text = line.get(index).text;
                    ln.reset().a(StringUtils.rightPad(text, line.get(index).width));
                    if (index == line.size() - 1) {
                        ln.fgYellow().a(" |");
                    }
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
        int currentLine;

        private Cell(final String line, final int cellWidth) {
            this.cellWidth = cellWidth;
            this.lines = ImmutableList.copyOf(line.split("(?<=\\G.{" + cellWidth + "})"));
            this.currentLine = 0;
        }


        String nextLine() {
            if (this.currentLine < this.lines.size()) {
                final String result = this.lines.get(this.currentLine);
                this.currentLine += 1;
                return result;
            } else {
                return null;
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
