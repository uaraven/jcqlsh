package net.ninjacat.cql.printer;

/**
 * Container for screen settings, such as result set printer and number of rows in a single fetch operation
 */
public class ScreenSettings {
    private ResultSetPrinterType resultSetPrinter;
    private int paging;

    public ScreenSettings(final ResultSetPrinterType resultSetPrinter, final int paging) {
        this.resultSetPrinter = resultSetPrinter;
        this.paging = paging;
    }

    public ResultSetPrinterType getResultSetPrinter() {
        return this.resultSetPrinter;
    }

    public void setResultSetPrinter(final ResultSetPrinterType resultSetPrinter) {
        this.resultSetPrinter = resultSetPrinter;
    }

    public int getPaging() {
        return this.paging;
    }

    public void setPaging(final int paging) {
        this.paging = paging;
    }
}
