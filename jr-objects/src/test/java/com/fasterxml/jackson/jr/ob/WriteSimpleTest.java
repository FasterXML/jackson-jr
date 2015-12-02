package com.fasterxml.jackson.jr.ob;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class WriteSimpleTest extends TestBase
{
    final static class POJO {
        public int value = 3;

        public POJO() { }
        public POJO(int v) { value = v; }
    }

    enum ABC { A, B, C; }

    final static class Address {
        public String name;

        public Address(String n) { name = n; }
    }
    
    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */
    
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
        stuff.put("d", UUID.fromString("8f88e079-7dc6-46f8-abfb-a533130f4ea0"));
        stuff.put("e", new URL("https://github.com/FasterXML/jackson-jr?a=x&b=y"));
        stuff.put("f", URI.create("https://github.com/FasterXML/jackson-jr?c=x&c=y"));

        assertEquals("{\"a\":15,\"b\":true,\"c\":\"foobar\",\"d\":\"8f88e079-7dc6-46f8-abfb-a533130f4ea0\"," +
                        "\"e\":\"https://github.com/FasterXML/jackson-jr?a=x&b=y\",\"f\":\"https://github.com/FasterXML/jackson-jr?c=x&c=y\"}",
                JSON.std.asString(stuff));
    }

    public void testSimpleIntContainers() throws Exception {
        assertEquals("[1,2,3]", JSON.std.asString(new int[] { 1, 2, 3 }));
        assertEquals("[1,2,3]", JSON.std.asString(new Integer[] { 1, 2, 3 }));
        List<Integer> list = new ArrayList<Integer>();
        list.add(4);
        list.add(-8);
        assertEquals("[4,-8]", JSON.std.asString(list));
    }

    public void testSimpleBooleanArray() throws Exception {
        assertEquals("[true,false]", JSON.std.asString(new boolean[] { true, false }));
        assertEquals("[true,false]", JSON.std.asString(new Boolean[] { true, false }));
    }
    
    public void testSimpleStringArray() throws Exception {
        assertEquals(aposToQuotes("['abc','def']"), JSON.std.asString(new String[] { "abc", "def" }));
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

        assertEquals(quote("B"), JSON.std.asString(ABC.B));
        assertEquals("1", JSON.std.with(Feature.WRITE_ENUMS_USING_INDEX).asString(ABC.B));
    }

    public void testUnknownType() throws Exception
    {
        try {
            String json = JSON.std.with(JSON.Feature.FAIL_ON_UNKNOWN_TYPE_WRITE)
                    .without(JSON.Feature.HANDLE_JAVA_BEANS)
                    .asString(new POJO());
            fail("Should have failed: instead got: "+json);
        } catch (Exception e) {
            verifyException(e, "unrecognized type");
            verifyException(e, "POJO");
        }
    }

    // For [jackson-jr#16]
    public void testTypedMaps() throws Exception
    {
        final Address from = new Address("xyz");
        final Map<String,Set<Address>> to = new HashMap<String,Set<Address>>();
        to.put("static_addr", new HashSet<Address>());
        to.get("static_addr").add(new Address("abc"));

        final   Map<String,Object> temp = new HashMap<String,Object>();
        temp.put("from", from);
        temp.put("TO", to);

        String json = JSON.std.asString(temp);

        assertNotNull(json);
        
        // and sanity check for back direction
        Map<?,?> map = JSON.std.mapFrom(json);
        assertNotNull(map);
        assertEquals(2, map.size());
    }

    // For [jackson-jr#29]
    public void testSimpleDates() throws Exception
    {
        final Date input = new Date(0L);
        JSON j = JSON.std;
        
        assertFalse(j.isEnabled(Feature.WRITE_DATES_AS_TIMESTAMP));

        String json = j.asString(input);
        // What to test? For now, accept two variants we may get, depending
        // on timezone (which we can not, alas, control)
        if (!json.contains("Dec 31")
                && !json.contains("Jan 1")) {
            fail("Invalid output: "+json);
        }

        j = j.with(Feature.WRITE_DATES_AS_TIMESTAMP);
        assertTrue(j.isEnabled(Feature.WRITE_DATES_AS_TIMESTAMP));

        json = j.asString(input);
        assertEquals("0", json);
    }
}
