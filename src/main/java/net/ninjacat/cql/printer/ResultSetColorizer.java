package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import org.fusesource.jansi.Ansi;

public class ResultSetColorizer {

    Ansi value(final Ansi ansi, final ColumnDefinitions.Definition columnDef) {
        switch (columnDef.getType().getName()) {
            case INT:
            case BIGINT:
            case FLOAT:
            case DOUBLE:
            case VARINT:
            case TINYINT:
            case DECIMAL:
                return ansi.fgGreen();
            case UUID:
                return ansi.fgBrightGreen();
            case TEXT:
            case ASCII:
            case VARCHAR:
                return ansi.fgBrightCyan();
            default:
                return ansi.fg(Ansi.Color.WHITE);
        }
    }

    Ansi header(final Ansi ansi, final ColumnDefinitions.Definition columnDef) {
        return ansi.fgBrightBlue();
    }

    Ansi table(final Ansi ansi) {
        return ansi.fgYellow();
    }
}
