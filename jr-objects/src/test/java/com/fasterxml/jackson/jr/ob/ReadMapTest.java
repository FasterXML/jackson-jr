package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;

// for [jackson-jr#49], where `DeferredMap` explodes
public class ReadMapTest extends TestBase
{
    static class MapHolder {
        public Map<String, List<Integer>> stuff;
    }

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

    public void testMapOfLists() throws Exception
    {
        MapHolder m = JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .with(JSON.Feature.USE_FIELDS)
                .beanFrom(MapHolder.class,
                        aposToQuotes("{'stuff':{'a':[1, 2, 3], 'b' : [7, 2]}}"));
        Map<String, List<Integer>> map = m.stuff;
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(Arrays.asList(Integer.valueOf(7), Integer.valueOf(2)),
                map.get("b"));
    }

    public void testInvalidMapOfLists() throws Exception
    {
        try {
            JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .with(JSON.Feature.USE_FIELDS)
                .beanFrom(MapHolder.class,
                        aposToQuotes("{'stuff':[ 1 ]}"));
        } catch (JSONObjectException e) {
            verifyException(e, "Unexpected token START_ARRAY");
        }
    }
}
