package net.ninjacat.cql.shell;

/**
 * Thrown when internal shell failure happened, usually invalid command syntax
 */
class ShellException extends RuntimeException {
    ShellException(final String message) {
        super(message);
    }

    ShellException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
