package net.ninjacat.cql.copy;

import net.ninjacat.cql.shell.ShellException;

class CopyException extends ShellException {
    CopyException(final String message) {
        super(message);
    }

    CopyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
