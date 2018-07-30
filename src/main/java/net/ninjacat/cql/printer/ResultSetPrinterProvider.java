package net.ninjacat.cql.printer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.ninjacat.cql.ShellContext;

public class ResultSetPrinterProvider {

    private final LoadingCache<ResultSetPrinterType, ResultSetPrinter> printerCache;

    public ResultSetPrinterProvider(final ShellContext context) {
        this.printerCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ResultSetPrinterType, ResultSetPrinter>() {
                    @Override
                    public ResultSetPrinter load(final ResultSetPrinterType key) throws Exception {
                        switch (key) {
                            case NICE:
                            case FILE:
                            default:
                                return new NiceResultSetPrinter(context);
                        }
                    }
                });
    }

    public ResultSetPrinter get(final ResultSetPrinterType type) {
        return this.printerCache.getUnchecked(type);
    }
}
