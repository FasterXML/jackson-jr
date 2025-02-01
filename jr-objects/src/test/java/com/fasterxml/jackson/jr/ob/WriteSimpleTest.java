package com.fasterxml.jackson.jr.ob;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON.Feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteSimpleTest extends TestBase
{
    static class POJO {
        public int value = 3;

        public POJO() { }
        public POJO(int v) { value = v; }
    }

    enum ABC { A, B, C; }

    static class Address {
        public String name;

        public Address(String n) { name = n; }
    }

    static class PathWrapper {
        public Path path;

        public PathWrapper(Path p) {
            path = p;
        }
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    @Test
    public void testSimpleList() throws Exception
    {
        List<Object> stuff = new LinkedList<Object>();
        stuff.add("x");
        stuff.add(true);
        stuff.add(123);
        final String exp = "[\"x\",true,123]";
        assertEquals(exp, JSON.std.asString(stuff));
        assertEquals(exp, new String(JSON.std.asBytes(stuff), "ASCII"));

        StringWriter sw = new StringWriter();
        JSON.std.write(stuff, sw);
        assertEquals(exp, sw.toString());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JSON.std.write(stuff, bytes);
        assertEquals(exp, bytes.toString("UTF-8"));
    }

    @Test
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

    @Test
    public void testSimpleIntContainers() throws Exception {
        assertEquals("[1,2,3]", JSON.std.asString(new int[] { 1, 2, 3 }));
        assertEquals("[1,2,3]", JSON.std.asString(new Integer[] { 1, 2, 3 }));
        List<Integer> list = new ArrayList<Integer>();
        list.add(4);
        list.add(-8);
        assertEquals("[4,-8]", JSON.std.asString(list));
    }

    @Test
    public void testSimpleBooleanArray() throws Exception {
        assertEquals("[true,false]", JSON.std.asString(new boolean[] { true, false }));
        assertEquals("[true,false]", JSON.std.asString(new Boolean[] { true, false }));
    }
    
    @Test
    public void testSimpleStringArray() throws Exception {
        assertEquals(a2q("['abc','def']"), JSON.std.asString(new String[] { "abc", "def" }));
    }
    
    @Test
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

    @Test
    public void testKnownSimpleTypeURI() throws Exception
    {
        final String URL_STR = "http://fasterxml.com";
        final URI uri = new URI(URL_STR);
        assertEquals(q(URL_STR), JSON.std.asString(uri));
    }

    @Test
    public void testKnownSimpleTypeFile() throws Exception
    {
        final String PATH = "/foo/bar.txt";
        assertEquals(q(PATH),
                JSON.std.asString(new File(PATH)));
    }

    @Test
    public void testKnownSimpleTypePath() throws Exception
    {
        Path p = Paths.get(new URI("file:///foo/bar.txt"));
        assertEquals(q("/foo/bar.txt"), JSON.std.asString(p));

        assertEquals(a2q("{'path':'/foo/bar.txt'}"), JSON.std.asString(new PathWrapper(p)));
    }

    @Test
    public void testSimpleEnumTypes() throws Exception
    {
        assertEquals(q("B"), JSON.std.asString(ABC.B));
        assertEquals("1", JSON.std.with(Feature.WRITE_ENUMS_USING_INDEX).asString(ABC.B));
    }

    @Test
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
    @Test
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
}
