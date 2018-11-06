package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.cassandra.KeyType;
import org.fusesource.jansi.Ansi;

public class ResultSetColorizer {

    private final ShellContext context;

    public ResultSetColorizer(final ShellContext context) {
        this.context = context;
    }

    Ansi value(final Ansi ansi, final ColumnDefinitions.Definition columnDef) {
        switch (columnDef.getType().getName()) {
            case INT:
            case BIGINT:
            case FLOAT:
            case DOUBLE:
            case VARINT:
            case TINYINT:
            case DECIMAL:
                return ansi.fgBrightCyan();
            case UUID:
                return ansi.fgBrightGreen();
            case BLOB:
                return ansi.fgBright(Ansi.Color.YELLOW);
            case TIMESTAMP:
            case DATE:
            case TIME:
            case DURATION:
                return ansi.fgBright(Ansi.Color.BLUE);
            case TEXT:
            case ASCII:
            case VARCHAR:
            default:
                return ansi.fg(Ansi.Color.WHITE);
        }
    }

    Ansi header(final Ansi ansi, final ColumnDefinitions.Definition columnDef) {
        final KeyType type = this.context.getKeyTypeOfColumn(columnDef);
        switch (type) {
            case PartitionKey:
                return ansi.fgBrightRed();
            case ClusteringKey:
                return ansi.fgBrightCyan();
            case NoKey:
            default:
                return ansi.fgBrightBlue();
        }
    }

    Ansi table(final Ansi ansi) {
        return ansi.fgYellow();
    }
}
