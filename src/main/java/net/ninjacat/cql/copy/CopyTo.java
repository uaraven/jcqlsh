package net.ninjacat.cql.copy;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.GettableData;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CopyTo extends BaseCopy {

    private CSVPrinter printer;
    private ResultSet resultSet;
    private int columnCount;

    public CopyTo(final ShellContext context, final CqlCopyContext copyContext) {
        super(context, copyContext);
    }

    @Override
    protected Stream<? extends GettableData> getSourceStream() {
        return StreamSupport.stream(this.resultSet.spliterator(), false);
    }

    @Override
    protected void insertRow(final GettableData row) {
        final List<Object> data = IntStream.of(this.columnCount).mapToObj(row::getObject).collect(Collectors.toList());
        try {
            this.printer.printRecord(data);
        } catch (final Exception ex) {
            throw new CopyException("Writing to file failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void initialize() {
        if (getCopyContext().getFileName().size() > 1) {
            throw new CopyException("COPY TO supports only one file");
        }
        try {
            final BufferedWriter bos = Files.newBufferedWriter(Paths.get(getCopyContext().getFileName().get(0)));
            this.printer = getCopyContext().isUseConsoleForIo()
                    ? new CSVPrinter(System.out, CSVFormat.DEFAULT)
                    : new CSVPrinter(bos, CSVFormat.DEFAULT);

            final Select query = QueryBuilder.select(getCopyContext().getColumns()).from(getCopyContext().getTableName());
            final PreparedStatement preparedStatement = getContext().getSession().prepare(query)
                    .setConsistencyLevel(getContext().getConsistencyLevel())
                    .setSerialConsistencyLevel(getContext().getSerialConsistencyLevel());
            this.resultSet = getContext().getSession().execute(preparedStatement.bind());
            this.columnCount = this.resultSet.getColumnDefinitions().size();

            final List<String> headers = this.resultSet.getColumnDefinitions().asList().stream()
                    .map(ColumnDefinitions.Definition::getName)
                    .collect(Collectors.toList());

            this.printer.printRecords(headers);

        } catch (final IOException ex) {
            throw new CopyException("I/O Error " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void flush() {
        try {
            this.printer.close(true);
        } catch (final IOException ignored) {
        }
    }
}
