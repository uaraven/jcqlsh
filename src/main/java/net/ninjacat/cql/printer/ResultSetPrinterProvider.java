package net.ninjacat.cql.printer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.ninjacat.cql.ShellContext;

/**
 * Returns new ResultSetPrinter based on requested type
 */
public class ResultSetPrinterProvider {

    private final LoadingCache<ResultSetPrinterType, CqlResultPrinter> printerCache;
    private final ShellContext context;

    public ResultSetPrinterProvider(final ShellContext context) {
        this.context = context;
        this.printerCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ResultSetPrinterType, CqlResultPrinter>() {
                    @Override
                    public CqlResultPrinter load(final ResultSetPrinterType key) throws Exception {
                        switch (key) {
                            case EXPANDED:
                                return new ExpandedResultSetPrinter(context);
                            case TABLE:
                                return new NiceResultSetPrinter(context);
                            case COMPACT:
                                return new CompactResultSetPrinter(context);
                            case CSV:
                                return new CsvResultSetPrinter(context);
                            case FLAT:
                            default:
                                return new FlatResultSetPrinter(context);
                        }
                    }
                });
    }

    public CqlResultPrinter get(final ResultSetPrinterType type) {
        final ResultSetPrinterType correctedType = ShellContext.isRunningInTerminal() ? type : ResultSetPrinterType.FLAT;
        return this.printerCache.getUnchecked(correctedType);
    }
}
