package net.ninjacat.cql.printer;

import com.datastax.driver.core.ResultSet;
import org.fusesource.jansi.Ansi;

/**
 * Prints CQL ResultSet
 */
public interface CqlResultPrinter {
    void printResultSet(final ResultSet resultSet);

    /**
     * Escapes special characters in the text string
     *
     * @param text String to escape characters in
     * @return String with escaped characters
     */
    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    default String escapeText(final String text) {
        return text.replaceAll("\n", "\\n").replaceAll("\r", "\\r").replaceAll("\t", "\\t");
    }

    default Ansi separator(final Ansi ansi) {
        return ansi.fgYellow();
    }

    default Ansi header(final Ansi ansi) {
        return ansi.fgBrightBlue();
    }

    default Ansi string(final Ansi ansi) {
        return ansi.fg(Ansi.Color.WHITE);
    }

    default Ansi number(final Ansi ansi) {
        return ansi.fgGreen();
    }

    default Ansi uuid(final Ansi ansi) {
        return ansi.fgCyan();
    }

}
