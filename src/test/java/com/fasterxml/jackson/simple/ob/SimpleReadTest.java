package com.fasterxml.jackson.simple.ob;

import java.util.*;

import com.fasterxml.jackson.simple.ob.JSON.Feature;

public class SimpleReadTest extends TestBase
{
    public void testSimpleList() throws Exception
    {
        final String INPUT = "[1,2,3]";
        Object ob = JSON.std.fromJSON(INPUT);
        // default mapping should be to List:
        assertTrue(ob instanceof List);
        assertEquals(3, ((List<?>) ob).size());
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asJSONString(ob));

        // but same should be possible with explicit call as well
        List<Object> list = JSON.std.listFromJSON(INPUT);
        assertEquals(3, list.size());
        assertEquals(INPUT, JSON.std.asJSONString(list));
    }

    public void testSimpleArray() throws Exception
    {
        final String INPUT = "[true,\"abc\"]";

        // first: can explicitly request an array:
        Object ob = JSON.std.arrayFromJSON(INPUT);
        assertTrue(ob instanceof Object[]);
        assertEquals(2, ((Object[]) ob).length);
        assertEquals(INPUT, JSON.std.asJSONString(ob));

        // or by changing default mapping:
        ob = JSON.std.with(Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS).fromJSON(INPUT);
        assertTrue(ob instanceof Object[]);
        assertEquals(2, ((Object[]) ob).length);
        assertEquals(INPUT, JSON.std.asJSONString(ob));
}
    
    public void testSimpleMap() throws Exception
    {
        final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
        Object ob = JSON.std.fromJSON(INPUT);
        assertTrue(ob instanceof Map);
        assertEquals(3, ((Map<?,?>) ob).size());
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asJSONString(ob));
    }
}
