package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadIntArray7Test extends TestBase
{
    @Test
    public void testReadIntArray() throws Exception {
        final int[] input = new int[]{1, 2, 3, 25, 999};
        String json = JSON.std.asString(input);
        int[] result = JSON.std.beanFrom(int[].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadIntArray2() throws Exception {
        final int[][] input = new int[][]{{1, 2, 3, 25, 999},{456,678,789},{1},{},{1000,2000,3000}};
        String json = JSON.std.asString(input);
        int[][] result = JSON.std.beanFrom(int[][].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadIntArray3() throws Exception {
        final int[][][] input = new int[][][]{{{1, 2, 3, 25, 999},{6,7,3}},{{456}, {678, 789}},{},{{},{23}},{{}}};
        String json = JSON.std.asString(input);
        int[][][] result = JSON.std.beanFrom(int[][][].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadIntArrayWhenEmpty() throws Exception {
        final int[][][] input = new int[][][]{};
        String json = JSON.std.asString(input);
        int[][][] result = JSON.std.beanFrom(int[][][].class, json);
        assertArrayEquals(input, result);
    }
}
