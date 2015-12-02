package com.fasterxml.jackson.jr.failing;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;

// for [jackson-jr#21]
public class ReadEnumMap21Test extends TestBase
{
    enum DEF { D, E, F; }

    static class WithEnumMap {
        private Map<DEF, String> values;

        WithEnumMap() { }
        public WithEnumMap(DEF key, String value) {
            values = new LinkedHashMap<DEF,String>();
            values.put(key, value);
        }

        public Map<DEF, String> getValues() { return values; }
        public void setValues(Map<DEF, String> v) { values = v; }
    }
 
    // [issue#21]
    public void testMapWithEnumKey() throws Exception
    {
        WithEnumMap input = new WithEnumMap(DEF.E, "bar");
        // verify serialization, should be ok:
        String json = JSON.std.asString(input);
        assertEquals(aposToQuotes("{'values':{'E':'bar'}}"), json);

        // and then get it back too
        WithEnumMap result = JSON.std.beanFrom(WithEnumMap.class, json);
        assertNotNull(result);
        Map<DEF, String> map = result.getValues();
        assertNotNull(map);
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals("bar", entry.getValue());
        if (!(entry.getKey() instanceof DEF)) {
            fail("Expected key to be of type ABC, is: "+entry.getKey().getClass().getName());
        }
        assertEquals(DEF.E, entry.getKey());
    }
}
