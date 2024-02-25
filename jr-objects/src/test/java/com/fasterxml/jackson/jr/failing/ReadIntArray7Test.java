package com.fasterxml.jackson.jr.failing;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;

import static org.junit.Assert.assertArrayEquals;

public class ReadIntArray7Test extends TestBase {
    public void testReadIntArray() throws Exception {
        final int[] input = new int[]{1, 2, 3, 25, 999};
        String json = JSON.std.asString(input);
        int[] result = JSON.std.beanFrom(int[].class, json);
        assertArrayEquals(input, result);
    }
}
