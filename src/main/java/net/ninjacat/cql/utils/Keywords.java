package net.ninjacat.cql.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import net.ninjacat.cql.parser.CqlTokenizer;

import java.io.IOException;
import java.util.Set;

public final class Keywords {

    private Keywords() {
    }

    public static Set<String> readResource(final String resourceName) {
        try {
            return ImmutableSet.copyOf(Resources.readLines(CqlTokenizer.class.getResource(resourceName), Charsets.UTF_8));
        } catch (final IOException e) {
            return ImmutableSet.of();
        }
    }
}
