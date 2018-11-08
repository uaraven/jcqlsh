package net.ninjacat.cql.copy;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

public abstract class BaseCopy {
    @Getter
    private final String file;
    @Getter
    private final Map<CopyOption, String> options;

    BaseCopy(final String file, final Map<CopyOption, String> options) {
        this.file = file;
        this.options = ImmutableMap.copyOf(options);
    }

    public abstract void copy();
}
