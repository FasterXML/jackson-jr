package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveArrayTest extends TestBase
{
    // Test all 7 primitive array types: boolean[], byte[], short[], int[], long[], float[], double[]
    // Also test char[] which is handled specially (as String)

    private final static String BOOLEAN_ARRAY_JSON = "[true,false,true,false,true]";
    private final static boolean[] BOOLEAN_ARRAY = new boolean[] { true, false, true, false, true };

    // Not yet implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testBooleanArrayRead() throws Exception {
        assertArrayEquals(BOOLEAN_ARRAY, JSON.std.beanFrom(boolean[].class, BOOLEAN_ARRAY_JSON));
    }

    @Test
    public void testBooleanArrayWrite() throws Exception {
        assertEquals(BOOLEAN_ARRAY_JSON, JSON.std.asString(BOOLEAN_ARRAY));
    }
    
    // Special: uses Base64 encoding for byte arrays
    @Test
    public void testByteArrayReadWrite() throws Exception {
        final byte[] input = new byte[]{1, 2, 3, 127, -128};
        String json = JSON.std.asString(input);
        byte[] result = JSON.std.beanFrom(byte[].class, json);
        assertArrayEquals(input, result);
    }

    // Special: char[] is serialized as a String
    @Test
    public void testCharArray() throws Exception {
        final char[] input = new char[]{'a', 'b', 'c', 'X', 'Y', 'Z'};
        String json = JSON.std.asString(input);
        char[] result = JSON.std.beanFrom(char[].class, json);
        assertArrayEquals(input, result);
    }

    private final static String SHORT_ARRAY_JSON = "[1,2,3,32767,-32768]";
    private final static short[] SHORT_ARRAY = new short[] { 1, 2, 3, 32767, -32768 };

    // Not yet implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testShortArrayRead() throws Exception {
        assertArrayEquals(SHORT_ARRAY, JSON.std.beanFrom(short[].class, SHORT_ARRAY_JSON));
    }

    @Test
    public void testShortArrayWrite() throws Exception {
        assertEquals(SHORT_ARRAY_JSON, JSON.std.asString(SHORT_ARRAY));
    }

    private final static String INT_ARRAY_JSON = "[1,2,-2000,1000000,-999999999]";
    private final static int[] INT_ARRAY = new int[] {1,2,-2000,1000000,-999999999};

    @Test
    public void testIntArrayRead() throws Exception {
        assertArrayEquals(INT_ARRAY, JSON.std.beanFrom(int[].class, INT_ARRAY_JSON));
    }

    @Test
    public void testIntArrayWrite() throws Exception {
        assertEquals(INT_ARRAY_JSON, JSON.std.asString(INT_ARRAY));
    }

    private final static String LONG_ARRAY_JSON = "[1,-2,3,999999999999,-999999999999]";
    private final static long[] LONG_ARRAY = new long[] {1L,-2L,3L,999999999999L,-999999999999L};

    @Test
    public void testLongArrayRead() throws Exception {
        assertArrayEquals(LONG_ARRAY, JSON.std.beanFrom(long[].class, LONG_ARRAY_JSON));
    }

    @Test
    public void testLongArrayWrite() throws Exception {
        assertEquals(LONG_ARRAY_JSON, JSON.std.asString(LONG_ARRAY));
    }

    private final static String FLOAT_ARRAY_JSON = "[1.0,2.5,3.125,-5.5,0.0]";
    private final static float[] FLOAT_ARRAY = new float[] {1.0f, 2.5f, 3.125f, -5.5f, 0.0f};

    // Not yet implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testFloatArrayRead() throws Exception {
        assertArrayEquals(FLOAT_ARRAY, JSON.std.beanFrom(float[].class, FLOAT_ARRAY_JSON),
                0.00001f);
    }

    @Test
    public void testFloatArrayWrite() throws Exception {
        assertEquals(FLOAT_ARRAY_JSON, JSON.std.asString(FLOAT_ARRAY));
    }

    private final static String DOUBLE_ARRAY_JSON = "[0.5,-2.25,3.14159,-5.5,0.0]";
    private final static double[] DOUBLE_ARRAY = new double[] {0.5, -2.25, 3.14159, -5.5f, 0.0};

    // Not yet implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testDoubleArrayRead() throws Exception {
        assertArrayEquals(DOUBLE_ARRAY, JSON.std.beanFrom(double[].class, DOUBLE_ARRAY_JSON),
                0.00001);
    }

    @Test
    public void testDoubleArrayWrite() throws Exception {
        assertEquals(DOUBLE_ARRAY_JSON, JSON.std.asString(DOUBLE_ARRAY));
    }

    // Test empty arrays
    // Not yet implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testEmptyArrays() throws Exception {
        assertArrayEquals(new boolean[0], JSON.std.beanFrom(boolean[].class, "[]"));
        assertArrayEquals(new byte[0], JSON.std.beanFrom(byte[].class, "[]"));
        assertArrayEquals(new char[0], JSON.std.beanFrom(char[].class, "\"\""));
        assertArrayEquals(new short[0], JSON.std.beanFrom(short[].class, "[]"));
        assertArrayEquals(new int[0], JSON.std.beanFrom(int[].class, "[]"));
        assertArrayEquals(new long[0], JSON.std.beanFrom(long[].class, "[]"));
        assertArrayEquals(new float[0], JSON.std.beanFrom(float[].class, "[]"), 0.0f);
        assertArrayEquals(new double[0], JSON.std.beanFrom(double[].class, "[]"), 0.0);
    }

    // Test arrays as object fields
    public static class AllArraysBean {
        public boolean[] booleans;
        public byte[] bytes;
        public char[] chars;
        public short[] shorts;
        public int[] ints;
        public long[] longs;
        public float[] floats;
        public double[] doubles;
    }

    // Not yet fully implemented in Jackson-jr
    @JacksonTestFailureExpected
    @Test
    public void testArraysAsObjectFields() throws Exception {
        AllArraysBean input = new AllArraysBean();
        input.booleans = new boolean[]{true, false};
        input.bytes = new byte[]{1, 2, 3};
        input.chars = new char[]{'a', 'b'};
        input.shorts = new short[]{10, 20};
        input.ints = new int[]{100, 200};
        input.longs = new long[]{1000L, 2000L};
        input.floats = new float[]{1.5f, 2.5f};
        input.doubles = new double[]{10.5, 20.5};

        String json = JSON.std.asString(input);
        AllArraysBean result = JSON.std.beanFrom(AllArraysBean.class, json);

        assertArrayEquals(input.booleans, result.booleans);
        assertArrayEquals(input.bytes, result.bytes);
        assertArrayEquals(input.chars, result.chars);
        assertArrayEquals(input.shorts, result.shorts);
        assertArrayEquals(input.ints, result.ints);
        assertArrayEquals(input.longs, result.longs);
        assertArrayEquals(input.floats, result.floats, 0.0001f);
        assertArrayEquals(input.doubles, result.doubles, 0.0000001);
    }
}