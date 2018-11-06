package net.ninjacat.cql;

import com.datastax.driver.core.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.ninjacat.cql.cassandra.ColumnId;
import net.ninjacat.cql.cassandra.KeyType;
import net.ninjacat.cql.printer.ResultSetColorizer;
import net.ninjacat.cql.printer.ResultSetPrinterType;
import net.ninjacat.cql.printer.ScreenSettings;
import net.ninjacat.cql.shell.ShellException;
import net.ninjacat.smooth.utils.Try;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Context of the shell. Contains terminal, KeyspaceTable session and gives access to {@link PrintWriter}
 */
public class ShellContext {
    private final Terminal terminal;
    private final Session session;
    private final ScreenSettings screenSettings;
    private final ResultSetColorizer resultColorizer;
    private final LoadingCache<ColumnId, KeyType> keyTypeCache;
    private boolean tracingEnabled;

    private ConsistencyLevel consistencyLevel;
    private ConsistencyLevel serialConsistencyLevel;

    ShellContext(final Terminal terminal, final Session session) {
        this.terminal = terminal;
        this.session = session;
        this.consistencyLevel = ConsistencyLevel.ONE;
        this.serialConsistencyLevel = ConsistencyLevel.SERIAL;
        this.tracingEnabled = false;
        this.resultColorizer = new ResultSetColorizer(this);
        this.screenSettings = new ScreenSettings(ResultSetPrinterType.COMPACT, terminal.getType().startsWith("dumb") ? 40 : terminal.getHeight());
        this.keyTypeCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<ColumnId, KeyType>() {
                    @Override
                    public KeyType load(@Nonnull final ColumnId key) {
                        return loadColumnKeyType(key);
                    }
                });
    }

    public static boolean isRunningInTerminal() {
        return System.console() != null;
    }

    public PrintWriter writer() {
        return this.terminal.writer();
    }

    public Terminal getTerminal() {
        return this.terminal;
    }

    public Session getSession() {
        return this.session;
    }

    public boolean isTracingEnabled() {
        return this.tracingEnabled;
    }

    public void setTracingEnabled(final boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    public void setConsistencyLevel(final ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public ConsistencyLevel getSerialConsistencyLevel() {
        return this.serialConsistencyLevel;
    }

    public void setSerialConsistencyLevel(final ConsistencyLevel serialConsistencyLevel) {
        this.serialConsistencyLevel = serialConsistencyLevel;
    }

    public boolean isPagingEnabled() {
        return this.screenSettings.isPagingEnabled();
    }

    public void setPagingEnabled(final boolean pagingEnabled) {
        this.screenSettings.setPagingEnabled(pagingEnabled);
    }

    public ResultSetPrinterType getResultSetPrinter() {
        return this.screenSettings.getResultSetPrinter();
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.screenSettings.setResultSetPrinter(resultSetPrinter);
    }

    public ScreenSettings getScreenSettings() {
        return this.screenSettings;
    }

    public ResultSetColorizer getResultColorizer() {
        return this.resultColorizer;
    }

    public boolean waitForKeypress() {
        final LineReader reader = LineReaderBuilder.builder()
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .terminal(getTerminal())
                .build();
        try {
            reader.readLine((char) 0);
            return true;
        } catch (final UserInterruptException ex) {
            return false;
        } catch (final Exception e) {
            throw new ShellException("", e);
        }
    }

    public KeyType getKeyTypeOfColumn(final ColumnDefinitions.Definition columnDef) {
        final ColumnId columnId = ColumnId.fromColumnDef(columnDef);
        return Try.execute(() -> this.keyTypeCache.get(columnId)).get().or(KeyType.NoKey);
    }

    private KeyType loadColumnKeyType(final ColumnId columnId) {
        final TableMetadata table = this.session.getCluster().getMetadata().getKeyspace(columnId.getKeyspace()).getTable(columnId.getTable());
        final Set<String> partitionKey = table.getPartitionKey().stream().map(ColumnMetadata::getName).collect(Collectors.toSet());
        final Set<String> clusteringColumns = table.getClusteringColumns().stream().map(ColumnMetadata::getName).collect(Collectors.toSet());

        if (partitionKey.contains(columnId.getColumn())) {
            return KeyType.PartitionKey;
        }
        if (clusteringColumns.contains(columnId.getColumn())) {
            return KeyType.ClusteringKey;
        }
        return KeyType.NoKey;
    }
}
