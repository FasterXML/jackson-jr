package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ReadLongArray198Test extends TestBase
{
    private static final long[] LONGS = {
            0, -42, 1, 999, -10_000, Long.MIN_VALUE, Long.MAX_VALUE, -1
    };

    @Test
    public void testReadLongArray() throws Exception {
        final long[] input = LONGS;
        String json = JSON.std.asString(input);
        long[] result = JSON.std.beanFrom(long[].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadLongArray2() throws Exception {
        final long[][] input = new long[][]{LONGS,{456,678,789},{1},{},{1000,2000,3000}};
        String json = JSON.std.asString(input);
        long[][] result = JSON.std.beanFrom(long[][].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadLongArray3() throws Exception {
        final long[][][] input = new long[][][]{{LONGS,{6,7,3}},{{456}, {678, 789}},{},{{},{23}},{{}}};
        String json = JSON.std.asString(input);
        long[][][] result = JSON.std.beanFrom(long[][][].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadLongArrayWhenEmpty() throws Exception {
        final long[][][] input = new long[][][]{};
        String json = JSON.std.asString(input);
        long[][][] result = JSON.std.beanFrom(long[][][].class, json);
        assertArrayEquals(input, result);
    }
}
