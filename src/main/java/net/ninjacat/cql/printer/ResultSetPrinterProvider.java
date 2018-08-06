package net.ninjacat.cql.printer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.ninjacat.cql.ShellContext;

public class ResultSetPrinterProvider {

    private final LoadingCache<ResultSetPrinterType, ResultSetPrinter> printerCache;
    private final ShellContext context;

    public ResultSetPrinterProvider(final ShellContext context) {
        this.context = context;
        this.printerCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ResultSetPrinterType, ResultSetPrinter>() {
                    @Override
                    public ResultSetPrinter load(final ResultSetPrinterType key) throws Exception {
                        switch (key) {
                            case TABLE:
                                return new NiceResultSetPrinter(context);
                            case COMPACT:
                                return new CompactResultSetPrinter(context);
                            case FLAT:
                            default:
                                return new FlatResultSetPrinter(context);
                        }
                    }
                });
    }

    public ResultSetPrinter get(final ResultSetPrinterType type) {
        final ResultSetPrinterType correctedType = this.context.isRunningInTerminal() ? type : ResultSetPrinterType.FLAT;
        return this.printerCache.getUnchecked(correctedType);
    }
}
