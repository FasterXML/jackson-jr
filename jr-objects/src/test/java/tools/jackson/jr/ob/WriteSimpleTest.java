package tools.jackson.jr.ob;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.Test;

import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.jr.ob.JSON.Feature;

import static org.junit.jupiter.api.Assertions.*;

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

    
    private JSON json() {
        return JSON.builder(JsonFactory.builder().enable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
                .build()).build();

    }

    @Test
    public void testSimpleList() throws Exception
    {
        final JSON json = json();
        List<Object> stuff = new LinkedList<>();
        stuff.add("x");
        stuff.add(true);
        stuff.add(123);
        final String exp = "[\"x\",true,123]";
        assertEquals(exp, json.asString(stuff));
        assertEquals(exp, new String(json.asBytes(stuff), "ASCII"));

        StringWriter sw = new StringWriter();
        json.write(stuff, sw);
        assertEquals(exp, sw.toString());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        json.write(stuff, bytes);
        assertEquals(exp, bytes.toString("UTF-8"));
    }

    @Test
    public void testSimpleMap() throws Exception
    {
        final JSON json = json();
        Map<String,Object> stuff = new LinkedHashMap<>();
        stuff.put("a", 15);
        stuff.put("b", Boolean.TRUE);
        stuff.put("c", "foobar");
        stuff.put("d", UUID.fromString("8f88e079-7dc6-46f8-abfb-a533130f4ea0"));
        stuff.put("e", new URL("https://github.com/FasterXML/jackson-jr?a=x&b=y"));
        stuff.put("f", URI.create("https://github.com/FasterXML/jackson-jr?c=x&c=y"));

        assertEquals(a2q(
                "{'a':15,'b':true,'c':'foobar','d':'8f88e079-7dc6-46f8-abfb-a533130f4ea0',"
                +"'e':'https:\\/\\/github.com\\/FasterXML\\/jackson-jr?a=x&b=y',"
                +"'f':'https:\\/\\/github.com\\/FasterXML\\/jackson-jr?c=x&c=y'}"),
                json.asString(stuff));
    }

    @Test
    public void testSimpleIntContainers() throws Exception {
        final JSON json = json();
        assertEquals("[1,2,3]", json.asString(new int[] { 1, 2, 3 }));
        assertEquals("[1,2,3]", json.asString(new Integer[] { 1, 2, 3 }));
        List<Integer> list = new ArrayList<>();
        list.add(4);
        list.add(-8);
        assertEquals("[4,-8]", json.asString(list));
    }

    @Test
    public void testSimpleBooleanArray() throws Exception {
        final JSON json = json();
        assertEquals("[true,false]", json.asString(new boolean[] { true, false }));
        assertEquals("[true,false]", json.asString(new Boolean[] { true, false }));
    }

    @Test
    public void testSimpleStringArray() throws Exception {
        final JSON json = json();
        assertEquals(a2q("['abc','def']"), json.asString(new String[] { "abc", "def" }));
    }
    
    @Test
    public void testNest() throws Exception
    {
        final JSON json = json();
        Map<String,Object> stuff = new LinkedHashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(123);
        list.add(456);
        stuff.put("first", list);
        Map<String,Object> second = new LinkedHashMap<>();
        stuff.put("second", second);
        second.put("foo", "bar");
        second.put("bar", new ArrayList<>());

        assertEquals("{\"first\":[123,456],\"second\":{\"foo\":\"bar\",\"bar\":[]}}",
                json.asString(stuff));
    }

    @Test
    public void testKnownSimpleTypeURI() throws Exception
    {
        final JSON json = json();
        final String URL_STR = "http://fasterxml.com";
        final URI uri = new URI(URL_STR);
        assertEquals(q("http:\\/\\/fasterxml.com"),
                json.asString(uri));
    }

    @Test
    public void testKnownSimpleTypeFile() throws Exception
    {
        final JSON json = json();
        final String PATH = "/foo/bar.txt";
        assertEquals(q("\\/foo\\/bar.txt"),
                json.asString(new File(PATH)));
    }

    @Test
    public void testKnownSimpleTypePath() throws Exception
    {
        final JSON json = json();
        Path p = Paths.get(new URI("file:///foo/bar.txt"));
        assertEquals(q("\\/foo\\/bar.txt"), json.asString(p));

        assertEquals(a2q("{'path':'\\/foo\\/bar.txt'}"), json.asString(new PathWrapper(p)));
    }

    @Test
    public void testSimpleEnumTypes() throws Exception
    {
        final JSON json = json();
        assertEquals(q("B"), json.asString(ABC.B));
        assertEquals("1", json.with(Feature.WRITE_ENUMS_USING_INDEX).asString(ABC.B));
    }

    @Test
    public void testUnknownType() throws Exception
    {
        final JSON json = json();
        try {
            String jsonStr = json.with(JSON.Feature.FAIL_ON_UNKNOWN_TYPE_WRITE)
                    .without(JSON.Feature.HANDLE_JAVA_BEANS)
                    .asString(new POJO());
            fail("Should have failed: instead got: "+jsonStr);
        } catch (Exception e) {
            verifyException(e, "unrecognized type");
            verifyException(e, "POJO");
        }
    }

    // For [jackson-jr#16]
    @Test
    public void testTypedMaps() throws Exception
    {
        final JSON json = json();
        final Address from = new Address("xyz");
        final Map<String,Set<Address>> to = new HashMap<>();
        to.put("static_addr", new HashSet<>());
        to.get("static_addr").add(new Address("abc"));

        final   Map<String,Object> temp = new HashMap<>();
        temp.put("from", from);
        temp.put("TO", to);

        String jsonStr = json.asString(temp);

        assertNotNull(jsonStr);
        
        // and sanity check for back direction
        Map<?,?> map = json.mapFrom(jsonStr);
        assertNotNull(map);
        assertEquals(2, map.size());
    }
}
