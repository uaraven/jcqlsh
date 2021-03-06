package net.ninjacat.cql.printer;

/**
 * Supported types of {@link ResultSetPrinter}
 */
public enum ResultSetPrinterType {
    /**
     * Formats result set in a table that fits on the screen. Long strings will occupy multiple rows
     */
    TABLE,
    /**
     * Similar to CQLSH, table width is not limited by terminal, column width is determined per page
     */
    FLAT,
    /**
     * Similar to {@link #TABLE} but will truncate long strings after first line
     */
    COMPACT,
    /**
     * Comma separated values
     */
    CSV,

    /**
     * Each row is printed vertically with each column on its own line
     */
    EXPANDED;
}
