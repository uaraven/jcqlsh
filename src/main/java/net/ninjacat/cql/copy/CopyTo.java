package net.ninjacat.cql.copy;

import com.datastax.driver.core.ResultSet;

import java.util.Map;

public class CopyTo extends BaseCopy {
    private final ResultSet resultSet;

    public CopyTo(final ResultSet resultSet, final String file, final Map<CopyOption, String> options) {
        super(file, options);
        this.resultSet = resultSet;
    }

    @Override
    public void copy() {

    }
}
