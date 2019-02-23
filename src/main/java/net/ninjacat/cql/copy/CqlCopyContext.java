package net.ninjacat.cql.copy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import net.ninjacat.cql.shell.ShellException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains all settings for COPY operation
 */
@Data
@Builder
public class CqlCopyContext {
    private String tableName;
    @Builder.Default()
    private List<String> columnNames = ImmutableList.of();
    private CopyDirection direction;
    @Builder.Default()
    private boolean useConsoleForIo = false;
    @Builder.Default()
    private List<String> fileName = ImmutableList.of();
    @Builder.Default()
    private Map<CopyOption, String> options = ImmutableMap.of();

    /**
     * Validates that different copy settings do not contradict each other. Throws {@link ShellException} in case of
     * validation failure
     *
     * @return This instance of CqlCopyContext
     */
    public CqlCopyContext validate() {
        final String failedOptions = this.options.keySet().stream()
                .filter(it -> !it.isValidFor(this.direction))
                .map(CopyOption::name)
                .collect(Collectors.joining(", "));

        if (!failedOptions.isEmpty()) {
            throw new ShellException("Invalid options " + failedOptions + " for COPY " + this.direction);
        }
        return this;
    }

    public String[] getColumns() {
        return this.columnNames.toArray(new String[0]);
    }
}
