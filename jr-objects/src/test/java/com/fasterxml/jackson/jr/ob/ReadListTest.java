package com.fasterxml.jackson.jr.ob;

import java.util.List;
import java.util.Map;

public class ReadListTest extends TestBase
{
    static class ListHolder {
        public List<Map<String, Integer>> stuff;
    }

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
