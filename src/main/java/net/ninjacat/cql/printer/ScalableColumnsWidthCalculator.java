package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import net.ninjacat.cql.ShellContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Trait that provides column width calculations for ResultSetPrinters with flexible-width columns
 */
@SuppressWarnings({"UnstableApiUsage", "InterfaceMayBeAnnotatedFunctional"})
public interface ScalableColumnsWidthCalculator {

    Set<DataType> FLEX_TYPES = ImmutableSet.of(
            DataType.ascii(),
            DataType.text(),
            DataType.varchar(),
            DataType.blob()
    );


    default List<Integer> columnWidths(final ShellContext context, final ColumnDefinitions columns, final List<Row> rows) {
        final int separatorOverhead = (columns.size() - 1) * 3 - 1;

        final IntStream defaultColumnWidths = columns.asList().stream().mapToInt(def -> def.getName().length());

        final int totalWidth = context.getTerminal().getWidth() - separatorOverhead;

        final List<Integer> columnWidths = rows.stream().map(
                row -> IntStream.range(0, columns.size())
                        .map(idx -> getText(row, idx).length()))
                .reduce(defaultColumnWidths,
                        (is1, is2) -> Streams.zip(is1.boxed(), is2.boxed(), Math::max).mapToInt(Integer::intValue))
                .boxed()
                .collect(Collectors.toList());

        final int totalColumnWidths = columnWidths.stream().mapToInt(i -> i).sum();

        if (totalColumnWidths < totalWidth) {
            return columnWidths;
        } else {
            final List<Integer> result = IntStream.range(0, columns.size()).mapToObj(it -> 0).collect(Collectors.toList());

            final List<IndexedWidth> sortedFlexCols = IntStream.range(0, columnWidths.size())
                    .mapToObj(idx -> new IndexedWidth(idx, columnWidths.get(idx), FLEX_TYPES.contains(columns.getType(idx))))
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
            int flexColumnWs = totalWidth - fixedColumnWs;
            int widthLeft = totalWidth - fixedColumnWs;

            final int threshold = (int) (widthLeft / sortedFlexCols.stream().filter(iw -> iw.flexible).count());
            for (final IndexedWidth iw : sortedFlexCols) {
                if (!iw.flexible || iw.width < threshold) {
                    result.set(iw.index, iw.width);
                    if (iw.flexible) {
                        widthLeft -= iw.width;
                        flexColumnWs -= iw.width;
                    }
                } else {
                    final int newW = iw.width / widthLeft * flexColumnWs - 3;
                    result.set(iw.index, newW);
                }
            }

            return result;
        }
    }

    String getText(Row row, int idx);

    final class IndexedWidth {
        private final int index;
        private final int width;
        private final boolean flexible;

        private IndexedWidth(final int index, final int width, final boolean flexible) {
            this.index = index;
            this.width = width;
            this.flexible = flexible;
        }
    }

}
