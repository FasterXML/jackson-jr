package com.fasterxml.jackson.simple.ob;

import java.util.*;

public class SimpleReadTest extends TestBase
{
    public void testSimpleList() throws Exception
    {
        final String INPUT = "[1,2,3]";
        Object ob = JSON.std.fromJSON(INPUT);
        assertTrue(ob instanceof List);
        assertEquals(3, ((List<?>) ob).size());
        // actually, verify with write...
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
