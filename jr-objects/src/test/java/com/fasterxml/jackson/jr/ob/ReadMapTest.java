package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.api.MapBuilder;

// for [jackson-jr#49], where `DeferredMap` explodes
public class ReadMapTest extends TestBase
{
    static class MapHolder {
        public Map<String, List<Integer>> stuff;
    }

    static class TreeMapBuilder extends MapBuilder {
        protected TreeMap<String, Object> _map = new TreeMap<String, Object>();

        TreeMapBuilder(int features) {
            super(features, TreeMap.class);
        }

        @Override
        public MapBuilder newBuilder(int features) {
            return new TreeMapBuilder(features);
        }

        @Override
        public MapBuilder newBuilder(Class<?> mapImpl) {
            return this;
        }

        @Override
        public MapBuilder start() {
            return new TreeMapBuilder(_features);
        }

        @Override
        public MapBuilder put(String key, Object value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public Map<String, Object> build() {
            return _map;
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testMapOfLists() throws Exception
    {
        final String INPUT = aposToQuotes("{'stuff':{'a':[1, 2, 3], 'b' : [7, 2]}}");
        final JSON j = JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .with(JSON.Feature.USE_FIELDS);
        MapHolder m = j.beanFrom(MapHolder.class, INPUT);
        _verifyMapOfLists(m.stuff);

        JsonParser p = parserFor(INPUT);
        m = j.beanFrom(MapHolder.class, p);
        _verifyMapOfLists(m.stuff);
        p.close();
    }

    private <T> void _verifyMapOfLists(Map<String, T> map) {
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

    public void testCustomMapBuilder() throws Exception
    {
        final JSON json = JSON.builder()
                .mapBuilder(new TreeMapBuilder(0))
                .build();
        Map<String, Object> map = json.mapFrom(a2q("{'a':1}"));
        assertEquals(TreeMap.class, map.getClass());

        map = json.mapFrom(a2q("{'b':1, 'a':2}"));
        assertEquals(TreeMap.class, map.getClass());
        assertEquals(Arrays.asList("a", "b"), new ArrayList<String>(map.keySet()));
    }

    public void testIssue49() throws Exception
    {
        for (int i : new int[] { 7, 99, 513, 1099, 3003, 5005, 10001, 90003, 111111 }) {
            _testLargeJson(i);
        }
    }

    private void _testLargeJson(int size) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < size; i++) {
            map.put("" + i, new HashMap<String, Object>());
        }
        String json = JSON.std.asString(map);
        Map<?,?> result = JSON.std.mapFrom(json);
        assertNotNull(result);
        assertEquals(size, result.size());

        JsonParser p = parserFor(json);
        result = JSON.std.mapFrom(p);
        assertNotNull(result);
        assertEquals(size, result.size());
        p.close();
    }
}
