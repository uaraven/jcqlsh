package net.ninjacat.cql.copy;

import com.datastax.driver.core.GettableData;
import net.ninjacat.cql.ShellContext;

import java.io.IOException;
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

    CqlCopyContext getCopyContext() {
        return this.copyContext;
    }

    protected abstract Stream<? extends GettableData> getSourceStream();

    protected abstract void insertRow(final GettableData row);

    protected abstract void initialize();

    protected abstract void flush();

    public void copy() {
        if (getCopyContext().getFileName().isEmpty() && !getCopyContext().isUseConsoleForIo()) {
            throw new CopyException("File must be specified");
        }
        initialize();
        getSourceStream().forEach(this::insertRow);
        flush();

    }

}
