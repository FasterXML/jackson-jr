package com.fasterxml.jackson.simple.ob;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class SimpleReadTest extends TestBase
{
    public void testSimpleList() throws Exception
    {
        final String INPUT = "[1,2,3]";
        Object ob = JSON.std.anyFrom(INPUT);
        // default mapping should be to List:
        assertTrue(ob instanceof List);
        assertEquals(3, ((List<?>) ob).size());
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asString(ob));

        // but same should be possible with explicit call as well
        List<Object> list = JSON.std.listFrom(INPUT);
        assertEquals(3, list.size());
        assertEquals(INPUT, JSON.std.asString(list));
    }

    public void testSimpleArray() throws Exception
    {
        final String INPUT = "[true,\"abc\"]";

        // first: can explicitly request an array:
        Object ob = JSON.std.arrayFrom(INPUT);
        assertTrue(ob instanceof Object[]);
        assertEquals(2, ((Object[]) ob).length);
        assertEquals(INPUT, JSON.std.asString(ob));

        // or by changing default mapping:
        ob = JSON.std.with(Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS).anyFrom(INPUT);
        assertTrue(ob instanceof Object[]);
        assertEquals(2, ((Object[]) ob).length);
        assertEquals(INPUT, JSON.std.asString(ob));
    }

    public void testSimpleMap() throws Exception
    {
        final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
        Object ob = JSON.std.anyFrom(INPUT);
        assertTrue(ob instanceof Map);
        assertEquals(3, ((Map<?,?>) ob).size());
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asString(ob));
    }

    public void testSimpleMixed() throws Exception
    {
        final String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
        Object ob = JSON.std.anyFrom(INPUT);
        assertTrue(ob instanceof Map);
        assertEquals(2, ((Map<?,?>) ob).size());
        Object list = (((Map<?,?>) ob).get("a"));
        assertTrue(list instanceof List<?>);
        
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asString(ob));
    }
}
