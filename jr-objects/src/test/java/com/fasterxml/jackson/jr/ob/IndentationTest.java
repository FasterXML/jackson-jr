package com.fasterxml.jackson.jr.ob;

import java.util.*;

public class IndentationTest extends TestBase
{
    public void testSimpleList() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        
        // By default, no indentation
        assertEquals("{\"a\":1,\"b\":2}", JSON.std.asString(map));
        // but with simple change...
        assertEquals("{\n"
                +"  \"a\" : 1,\n"
                +"  \"b\" : 2\n"
                +"}",
                JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).asString(map));
    }
}
