package net.ninjacat.cql.utils;

/**
 * Wrapper around table name. May contain keyspace name separated with '.' from table name
 */
public final class KeyspaceTable {

    private final String keyspace;
    private final String table;

    private KeyspaceTable(final String combined) {
        final String[] pair = combined.split("\\.");
        if (pair.length > 1) {
            this.keyspace = pair[0];
            this.table = pair[1];
        } else {
            this.keyspace = "";
            this.table = combined;
        }
    }

    private KeyspaceTable(final String keyspace, final String table) {
        this.keyspace = keyspace;
        this.table = table;
    }

    public static KeyspaceTable of(final String fullName) {
        return new KeyspaceTable(fullName);
    }

    public static KeyspaceTable of(final String keyspace, final String table) {
        return new KeyspaceTable(keyspace, table);
    }


    public String getKeyspace() {
        return this.keyspace;
    }

    public String getTable() {
        return this.table;
    }

    public String getFullName() {
        if (this.keyspace.isEmpty()) {
            return this.table;
        } else {
            return String.format("%s.%s", this.keyspace, this.table);
        }
    }

    public boolean hasKeyspace() {
        return !this.keyspace.isEmpty();
    }
}
