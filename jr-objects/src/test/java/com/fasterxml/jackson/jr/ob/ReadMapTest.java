package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;

// for [jackson-jr#49], where `DeferredMap` explodes
public class ReadMapTest extends TestBase
{
    public void testIssues49() throws Exception
    {
        for (int i : new int[] { 7, 99, 513, 1099, 3003, 5005, 10001, 90003, 111111 }) {
            testLargeJson(i);
        }
    }

    private void testLargeJson(int size) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < size; i++) {
            map.put("" + i, new HashMap<String, Object>());
        }
        String json = JSON.std.asString(map);
        Map<?,?> result = JSON.std.mapFrom(json);
        assertNotNull(result);
        assertEquals(size, result.size());
    }
}
