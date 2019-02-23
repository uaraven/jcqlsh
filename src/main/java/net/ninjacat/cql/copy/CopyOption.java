package net.ninjacat.cql.copy;

/**
 * All options for COPY TO/FROM operations
 */
public enum CopyOption {
    DELIMITER(true, true),
    QUOTE(true, true),
    ESCAPE(true, true),
    HEADER(true, true),
    NULL(true, true),
    DATETIMEFORMAT(true, true),
    MAXATTEMPTS(true, true),
    REPORTFREQUENCY(true, true),
    DECIMALSEP(true, true),
    THOUSANDSSEP(true, true),
    BOOLSTYLE(true, true),
    NUMPROCESSES(true, true),
    //    CONFIGFILE(true, true),
    RATEFILE(true, true),
    CHUNKSIZE(true, false),
    INGESTRATE(true, false),
    MAXBATCHSIZE(true, false),
    MINBATCHSIZE(true, false),
    MAXROWS(true, false),
    SKIPROWS(true, false),
    SKIPCOLS(true, false),
    MAXPARSEERRORS(true, false),
    MAXINSERTERRORS(true, false),
    ERRFILE(true, false),
    TTL(true, false),
    ENCODING(false, true),
    PAGESIZE(false, true),
    PAGETIMEOUT(false, true),
    BEGINTOKEN(false, true),
    ENDTOKEN(false, true),
    MAXREQUESTS(false, true),
    MAXOUTPUTSIZE(false, true);

    private final boolean to;
    private final boolean from;

    CopyOption(final boolean from, final boolean to) {
        this.from = from;
        this.to = to;
    }

    public boolean isValidFor(final CopyDirection direction) {
        return (direction == CopyDirection.FROM && isFrom()) || (direction == CopyDirection.TO && isTo());
    }

    public boolean isTo() {
        return this.to;
    }

    public boolean isFrom() {
        return this.from;
    }
}
