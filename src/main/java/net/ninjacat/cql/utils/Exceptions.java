package net.ninjacat.cql.utils;

import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

public final class Exceptions {

    private Exceptions() {
    }

    public static String toAnsiException(final Throwable thr) {
        final Ansi a = ansi().fg(Ansi.Color.RED)
                .bold().a(thr.getMessage()).boldOff()
                .a("\nCaused by ").a(thr.getCause().toString())
                .a("\n at ");

        boolean firstLine = true;
        for (final StackTraceElement trace : thr.getStackTrace()) {
            if (firstLine) {
                firstLine = false;
            } else {
                a.a("    ");
            }
            a.a(String.format("%s.%s in %s:%d%n",
                    trace.getClassName(), trace.getMethodName(), trace.getFileName(), trace.getLineNumber()));
        }
        return a.reset().toString();
    }
}
