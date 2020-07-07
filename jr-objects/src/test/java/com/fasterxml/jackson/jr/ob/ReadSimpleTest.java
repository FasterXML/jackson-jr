package com.fasterxml.jackson.jr.ob;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class ReadSimpleTest extends TestBase
{
    enum ABC { A, B, C; }

    /*
    /**********************************************************************
    /* Tests for Lists/Collections
    /**********************************************************************
     */
    
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

    static class DateWrapper {
        public Date value;
    }
    
    /*
    /**********************************************************************
    /* Tests for arrays
    /**********************************************************************
     */
    
    public void testSimpleArray() throws Exception
    {
        _testArray("[true,\"abc\",3]", 3);
    }

    public void testEmptyArray() throws Exception
    {
        _testArray("[]", 0);
    }

    // separate tests since code path differs
    public void testSingleElementArray() throws Exception
    {
        _testArray("[12]", 1);
    }

    private void _testArray(String input, int expCount) throws Exception
    {
        Object ob;

        // first: can explicitly request an array:
        ob = JSON.std.arrayFrom(input);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));

        // or, with
        ob = JSON.std
                .arrayOfFrom(Object.class, input);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));

        ob = JSON.std
              .with(JSON.Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS)
              .arrayOfFrom(Object.class, input);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));
        
        // or by changing default mapping:
        ob = JSON.std.with(Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS).anyFrom(input);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));
    }

    /*
    /**********************************************************************
    /* Tests for Maps
    /**********************************************************************
     */
    
    public void testSimpleMap() throws Exception
    {
        final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
        Object ob = JSON.std.anyFrom(INPUT);
        assertTrue(ob instanceof Map);
        assertEquals(3, ((Map<?,?>) ob).size());
        // actually, verify with write...
        assertEquals(INPUT, JSON.std.asString(ob));

        // or, via explicit Map read
        Map<String,Object> stuff = JSON.std.mapFrom(INPUT);
        assertEquals(3, stuff.size());
    }

    /*
    /**********************************************************************
    /* Null tests for Scalars
    /**********************************************************************
     */

    // 07-Jul-2020, tatu: Should be able to check but for 2.11 can't support
    public void testNullForMiscNumbers() throws Exception {
        /*
        assertNull(JSON.std.beanFrom(Integer.class," null "));
        assertNull(JSON.std.beanFrom(Long.class," null "));
        assertNull(JSON.std.beanFrom(Double.class," null "));

        assertNull(JSON.std.beanFrom(BigInteger.class," null "));
        assertNull(JSON.std.beanFrom(BigDecimal.class," null "));
         */
    }

    public void testNullForMiscScalars() throws Exception {
        assertNull(JSON.std.beanFrom(Date.class," null "));
        assertNull(JSON.std.beanFrom(Calendar.class," null "));

        assertNull(JSON.std.beanFrom(String.class," null "));
        assertNull(JSON.std.beanFrom(Class.class," null "));
        assertNull(JSON.std.beanFrom(File.class," null "));
        assertNull(JSON.std.beanFrom(URL.class," null "));
        assertNull(JSON.std.beanFrom(URI.class," null "));
    }

    public void testNullForScalarProperties() throws Exception {
        DateWrapper w = JSON.std.beanFrom(DateWrapper.class, aposToQuotes("{'value':null}"));
        assertNotNull(w);
        assertNull(w.value);
    }

    /*
    /**********************************************************************
    /* Other tests
    /**********************************************************************
     */

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

        // `null`s ok too
        assertNull(JSON.std.beanFrom(ABC.class, "null"));

        // But not others...
        try {
            JSON.std.beanFrom(ABC.class, " true ");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Can not read Enum ");
            verifyException(e, "from `true`");
        }
    }
}
