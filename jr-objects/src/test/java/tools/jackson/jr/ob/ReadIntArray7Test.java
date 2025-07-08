package tools.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ReadIntArray7Test extends TestBase
{
    private static final int[] INTS = {
            1, -42, 0, 999, -10_000, Integer.MIN_VALUE, Integer.MAX_VALUE
    };

    @Test
    public void testReadIntArray() throws Exception {
        final int[] input = INTS;
        String json = JSON.std.asString(input);
        int[] result = JSON.std.beanFrom(int[].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadIntArray2() throws Exception {
        final int[][] input = new int[][]{INTS,{456,678,789},{1},{},{1000,2000,3000}};
        String json = JSON.std.asString(input);
        int[][] result = JSON.std.beanFrom(int[][].class, json);
        assertArrayEquals(input, result);
    }

    @Test
    public void testReadIntArray3() throws Exception {
        final int[][][] input = new int[][][]{{INTS,{6,7,3}},{{456}, {678, 789}},{},{{},{23}},{{}}};
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
