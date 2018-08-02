package net.ninjacat.cql.shell;

/**
 * Thrown when internal shell failure happened, usually invalid command syntax
 */
public class ShellException extends RuntimeException {
    public ShellException(final String message) {
        super(message);
    }

    public ShellException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
