package net.ninjacat.cql.copy;

import com.datastax.driver.core.GettableData;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import net.ninjacat.cql.ShellContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CopyTo extends BaseCopy {

    public CopyTo(final ShellContext context, final CqlCopyContext copyContext) {
        super(context, copyContext);
    }

    @Override
    protected Stream<? extends GettableData> getSourceStream() {
        final Select query = QueryBuilder.select(getCopyContext().getColumns()).from(getCopyContext().getTableName());
        final PreparedStatement preparedStatement = getContext().getSession().prepare(query)
                .setConsistencyLevel(getContext().getConsistencyLevel())
                .setSerialConsistencyLevel(getContext().getSerialConsistencyLevel());
        final ResultSet resultSet = getContext().getSession().execute(preparedStatement.bind());
        return StreamSupport.stream(resultSet.spliterator(), false);
    }

    @Override
    protected void insertRow(final GettableData row) {

    }

    @Override
    protected void initialize() {
        if (getCopyContext().getFileName().size() > 1) {
            throw new CopyException("COPY TO supports only one file");
        }
        new CSVPrinter(, CSVFormat.DEFAULT)
        if (getCopyContext().isUseConsoleForIo())
    }

    @Override
    protected void flush() {

    }
}
