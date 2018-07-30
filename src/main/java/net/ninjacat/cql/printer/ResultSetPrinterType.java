package net.ninjacat.cql.printer;

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
    COMPACT
}
