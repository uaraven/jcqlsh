package net.ninjacat.cql.copy;

import com.datastax.driver.core.GettableData;
import net.ninjacat.cql.ShellContext;

import java.util.stream.Stream;

public class CopyFrom extends BaseCopy {

    public CopyFrom(final ShellContext context, final CqlCopyContext copyContext) {
        super(context, copyContext);
    }

    @Override
    protected Stream<? extends GettableData> getSourceStream() {

        return null;
    }

    @Override
    protected void insertRow(final GettableData row) {

    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void flush() {

    }

}
