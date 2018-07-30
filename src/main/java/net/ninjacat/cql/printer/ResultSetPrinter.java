package net.ninjacat.cql.printer;

import com.datastax.driver.core.ResultSet;

public interface ResultSetPrinter {

    void printResultSet(final ResultSet resultSet);
}
