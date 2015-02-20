package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class ReadSimpleTest extends TestBase
{
    enum ABC { A, B, C; }

    static class WithEnumMap {
        private Map<ABC, String> values;

        WithEnumMap() { }
        public WithEnumMap(ABC key, String value) {
            values = new LinkedHashMap<ABC,String>();
            values.put(key, value);
        }

        public Map<ABC, String> getValues() { return values; }
        public void setValues(Map<ABC, String> v) { values = v; }
    }
    
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

    public void testSimpleEnums() throws Exception
    {
        // First using index
        ABC abc = JSON.std.beanFrom(ABC.class, String.valueOf(ABC.B.ordinal()));
        assertEquals(ABC.B, abc);

        // then from name
        abc = JSON.std.beanFrom(ABC.class, quote("C"));
        assertEquals(ABC.C, abc);
    }

    // [issue#21]
    public void testMapWithEnumKey() throws Exception
    {
        WithEnumMap input = new WithEnumMap(ABC.B, "bar");
        // verify serialization, should be ok:
        String json = JSON.std.asString(input);
        assertEquals(aposToQuotes("{'values':{'B':'bar'}}"), json);

        // and then get it back too
        WithEnumMap result = JSON.std.beanFrom(WithEnumMap.class, json);
        assertNotNull(result);
        Map<ABC, String> map = result.getValues();
        assertNotNull(map);
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals("bar", entry.getValue());
        if (!(entry.getKey() instanceof ABC)) {
            fail("Expected key to be of type ABC, is: "+entry.getKey().getClass().getName());
        }
        assertEquals(ABC.B, entry.getKey());
    }
}
