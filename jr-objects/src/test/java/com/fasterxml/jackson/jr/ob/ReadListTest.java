package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.ReadMapTest.TreeMapBuilder;
import com.fasterxml.jackson.jr.ob.api.CollectionBuilder;

public class ReadListTest extends TestBase
{
    static class ListHolder {
        public List<Map<String, Integer>> stuff;
    }

    static class LinkedListBuilder extends CollectionBuilder {
        LinkedList<Object> _set = new LinkedList<Object>();

        LinkedListBuilder(int features) {
            super(features, TreeSet.class);
        }
 
        @Override
        public CollectionBuilder newBuilder(int features) {
            return new LinkedListBuilder(features);
        }

        @Override
        public CollectionBuilder newBuilder(Class<?> collImpl) {
            return this;
        }

        @Override
        public CollectionBuilder start() {
            return new LinkedListBuilder(_features);
        }

        @Override
        public CollectionBuilder add(Object value) {
            _set.add(value);
            return this;
        }

        @Override
        public Collection<Object> buildCollection() {
            return _set;
        }
    }

    /*
    /**********************************************************************
    /* Tests for simple Lists
    /**********************************************************************
     */
    
    public void testSimpleList() throws Exception
    {
        final String INPUT = "[1,2,3]";

        _verifySimpleList(JSON.std.anyFrom(INPUT), INPUT);
        // but same should be possible with explicit call as well
        _verifySimpleList(JSON.std.listFrom(INPUT), INPUT);

        JsonParser p = parserFor(INPUT);
        _verifySimpleList(JSON.std.listFrom(p), INPUT);
        assertFalse(p.hasCurrentToken());
        p.close();
    }

    private void _verifySimpleList(Object ob, String asJson) throws Exception {
        // default mapping should be to List:
        assertTrue(ob instanceof List);
        assertEquals(3, ((List<?>) ob).size());
        // actually, verify with write...
        assertEquals(asJson, JSON.std.asString(ob));
    }

    /*
    /**********************************************************************
    /* Tests for List of Maps
    /**********************************************************************
     */
    
    public void testListOfMaps() throws Exception
    {
        ListHolder h = JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .with(JSON.Feature.USE_FIELDS)
                .beanFrom(ListHolder.class,
                        aposToQuotes("{'stuff':[{'a':4}, {'a':6}]}"));
        List<Map<String, Integer>> list = h.stuff;
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(6), list.get(1).get("a"));
    }

    public void testInvalidListOfMaps() throws Exception
    {
        try {
            JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .with(JSON.Feature.USE_FIELDS)
                .beanFrom(ListHolder.class,
                        aposToQuotes("{'stuff':{ 'a' : 3 }}"));
        } catch (JSONObjectException e) {
            verifyException(e, "Unexpected token START_OBJECT");
        }
    }

    /*
    /**********************************************************************
    /* Other tests
    /**********************************************************************
     */

    public void testCustomMapBuilder() throws Exception
    {
        final JSON json = JSON.builder()
                .collectionBuilder(new LinkedListBuilder(0))
                .build();
        Collection<Object> stuff = json.listFrom(a2q("['a']"));
        assertEquals(LinkedList.class, stuff.getClass());

        stuff = json.listFrom(a2q("['a', 'b', 'c']"));
        assertEquals(LinkedList.class, stuff.getClass());
        assertEquals(Arrays.asList("a", "b", "c"), stuff);
    }
}
