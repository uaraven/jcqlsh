package net.ninjacat.cql.utils;

import com.google.common.collect.ImmutableList;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public final class Utils {
    private Utils() {
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException ignored) {
        }
    }

    /**
     * Returns immutable slice of source list
     *
     * @param source Source list
     * @param <T>    Type of list elements
     * @return Slice of list.
     */
    public static <T> Slicer<T> slice(final List<T> source) {
        return new Slicer<T>(source);
    }

    public static final class Slicer<T> {
        private final List<T> source;
        private int startIndex;

        private Slicer(final List<T> source) {
            this.source = source;
        }

        public Slicer<T> from(final int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public List<T> to(final int lastIndex) {
            return slice(lastIndex + 1);
        }

        public List<T> till(final int lastIndex) {
            return slice(lastIndex);
        }

        public List<T> items(final int length) {
            final int lastIndex = this.startIndex + length;
            return slice(lastIndex + 1);
        }

        public List<T> tillEnd() {
            return slice(this.source.size());
        }

        private List<T> slice(final int lastIndex) {
            if (this.startIndex > lastIndex || this.startIndex >= this.source.size()) {
                return ImmutableList.of();
            }
            final int sliceStart = this.startIndex < 0 ? 0 : this.startIndex;
            final int sliceEnd = (lastIndex + 1) >= this.source.size() ? this.source.size() : lastIndex + 1;

            return this.source.subList(sliceStart, sliceEnd);
        }
    }
}
