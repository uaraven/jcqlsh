package net.ninjacat.cql.copy;

import com.datastax.driver.core.GettableData;
import net.ninjacat.cql.ShellContext;

import java.util.stream.Stream;

public abstract class BaseCopy {

    private ShellContext context;
    private final CqlCopyContext copyContext;

    BaseCopy(final ShellContext context, final CqlCopyContext copyContext) {
        this.context = context;
        this.copyContext = copyContext;
    }

    public ShellContext getContext() {
        return this.context;
    }

    public CqlCopyContext getCopyContext() {
        return this.copyContext;
    }

    protected abstract Stream<? extends GettableData> getSourceStream();

    protected abstract void insertRow(final GettableData row);

    protected abstract void initialize();
    protected abstract void flush();

    public void copy() {
        initialize();
        getSourceStream().forEach(this::insertRow);
        flush();
    }

}
