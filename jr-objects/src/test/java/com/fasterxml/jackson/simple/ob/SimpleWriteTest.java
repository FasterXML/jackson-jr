package com.fasterxml.jackson.simple.ob;

import java.io.File;
import java.net.URI;
import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;

public class SimpleWriteTest extends TestBase
{
    final static class POJO {
        public int value = 3;
    }
    
    public void testSimpleList() throws Exception
    {
        List<Object> stuff = new LinkedList<Object>();
        stuff.add("x");
        stuff.add(true);
        stuff.add(123);
        assertEquals("[\"x\",true,123]", JSON.std.asString(stuff));
    }

    public void testSimpleMap() throws Exception
    {
        Map<String,Object> stuff = new LinkedHashMap<String,Object>();
        stuff.put("a", 15);
        stuff.put("b", Boolean.TRUE);
        stuff.put("c", "foobar");
        
        assertEquals("{\"a\":15,\"b\":true,\"c\":\"foobar\"}",
                JSON.std.asString(stuff));
    }

    public void testNest() throws Exception
    {
        Map<String,Object> stuff = new LinkedHashMap<String,Object>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(123);
        list.add(456);
        stuff.put("first", list);
        Map<String,Object> second = new LinkedHashMap<String,Object>();
        stuff.put("second", second);
        second.put("foo", "bar");
        second.put("bar", new ArrayList<Object>());

        assertEquals("{\"first\":[123,456],\"second\":{\"foo\":\"bar\",\"bar\":[]}}",
                JSON.std.asString(stuff));
    }

    public void testKnownSimpleTypes() throws Exception
    {
        final String URL_STR = "http://fasterxml.com";
        assertEquals(quote(URL_STR),
                JSON.std.asString(new URI(URL_STR)));
        final String PATH = "/foo/bar.txt";
        assertEquals(quote(PATH),
                JSON.std.asString(new File(PATH)));
    }

    public void testUnnownType() throws Exception
    {
        try {
            String json = JSON.std.with(JSON.Feature.FAIL_ON_UNKNOWN_TYPE_WRITE).asString(new POJO());
            fail("Should have failed: instead got: "+json);
        } catch (Exception e) {
            verifyException(e, "unrecognized type");
            verifyException(e, "POJO");
        }
    }
}
