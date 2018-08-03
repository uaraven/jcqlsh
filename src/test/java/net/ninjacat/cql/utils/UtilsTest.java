package net.ninjacat.cql.utils;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;

public class UtilsTest {

    private static final ImmutableList<Integer> SOURCE = ImmutableList.of(0, 1, 2, 3, 4);

    @Test
    public void shouldSliceList() {
        final List<Integer> slice = Utils.slice(SOURCE).from(1).to(3);
        assertThat(slice, Matchers.contains(1, 2, 3));
    }
}