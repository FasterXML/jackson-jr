package com.fasterxml.jackson.simple.ob;

import java.util.*;

public class SimpleWriteTest extends TestBase
{
    public void testSimpleMap() throws Exception
    {
        Map<String,Object> stuff = new LinkedHashMap<String,Object>();
        stuff.put("a", 15);
        stuff.put("b", Boolean.TRUE);
        stuff.put("c", "foobar");
        
        assertEquals("{\"a\":15,\"b\":true,\"c\":\"foobar\"}",
                JSON.std.asJSONString(stuff));
    }
}
