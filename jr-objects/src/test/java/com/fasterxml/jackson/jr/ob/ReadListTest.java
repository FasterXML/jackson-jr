package com.fasterxml.jackson.jr.ob;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;

public class ReadListTest extends TestBase
{
    static class ListHolder {
        public List<Map<String, Integer>> stuff;
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
}
