package net.ninjacat.cql.utils;

import java.io.Closeable;
import java.io.IOException;

public final class Utils {
    private Utils() {
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException ignored) {
        }
    }
}
