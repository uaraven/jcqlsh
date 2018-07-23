package net.ninjacat.cql.shell;

/**
 * Thrown if failed to parse "describe" arguments
 */
public class DescribeException extends RuntimeException {
    public DescribeException(String message) {
        super(message);
    }

    public DescribeException(String message, Throwable cause) {
        super(message, cause);
    }
}
