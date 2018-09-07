package net.ninjacat.cql.printer;

import com.datastax.driver.core.ColumnDefinitions;
import org.fusesource.jansi.Ansi;

public class ResultSetHighligher {

    void highlightValue(final Ansi ansi, final Object value, final ColumnDefinitions.Definition columnDef) {
    }

    Ansi highlightHeader(final Ansi ansi, final ColumnDefinitions.Definition columnDef) {
        return ansi.fgBrightBlue();
    }

    Ansi hightlightTable(final Ansi ansi) {
        return ansi.fgYellow();
    }
}
