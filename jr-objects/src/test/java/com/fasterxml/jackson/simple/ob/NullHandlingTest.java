package com.fasterxml.jackson.simple.ob;

import java.util.LinkedHashMap;
import java.util.Map;

public class NullHandlingTest extends TestBase
{
    // Test to verify that outputting of nulls is configurable
    public void testMapNullEntries() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("a", 1);
        map.put("b", null);
        // By default we do NOT write null-valued entries:
        assertEquals("{\"a\":1}", JSON.std.asString(map));
        // but we can disable it easily
        assertEquals("{\"a\":1,\"b\":null}",
                JSON.std.with(JSON.Feature.WRITE_NULL_PROPERTIES).asString(map));
    }
}
