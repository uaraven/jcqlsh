package net.ninjacat.cql.cassandra;

import net.ninjacat.cql.ShellContext;
import net.ninjacat.cql.shell.ShellException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class CqlFileExecutor {

    private final ShellContext context;

    public CqlFileExecutor(final ShellContext context) {
        this.context = context;
    }

    public void execute(final Path source) {
        if (!Files.exists(source)) {
            throw new ShellException(String.format("Source file '%s' does not exist", source));
        }
        try {
            final String cqlSource = Files.readAllLines(source).stream().collect(Collectors.joining("\n"));
            final CqlParser cqlParser = CqlParser.forScript(cqlSource);

            final CqlExecutor executor = new CqlExecutor(this.context);

            for (final String statement : cqlParser.statements()) {
                executor.execute(statement);
            }
        } catch (final IOException ex) {
            throw new ShellException("Failed to read or parse source file: " + ex.getMessage(), ex);
        } catch (final Exception ex) {
            throw new ShellException("Failed to execute source file: " + ex.getMessage(), ex);
        }
    }


}
