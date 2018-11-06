package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.ColumnDefinitions;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnId {
    private String keyspace;
    private String table;
    private String column;

    public static ColumnId fromColumnDef(ColumnDefinitions.Definition columnDef) {
        return new ColumnId(columnDef.getKeyspace(), columnDef.getTable(), columnDef.getName());
    }
}
