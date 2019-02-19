package net.ninjacat.cql.copy;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
class CqlCopyData {
    private String tableName;
    private List<String> columnNames;
    private CopyDirection direction;
    @Builder.Default()
    private boolean useConsoleForIo = false;
    private List<String> fileName;
    private Map<CopyOption, String> options;
}
