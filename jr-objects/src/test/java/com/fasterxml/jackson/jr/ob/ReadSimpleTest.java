package com.fasterxml.jackson.jr.ob;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class ReadSimpleTest extends TestBase
{
    static class BooleanWrapper {
        public boolean value;
    }

    static class IntArrayWrapper {
        public int[] value;
    }

    static class TreeWrapper {
        public TreeNode value;
    }

    /*
    /**********************************************************************
    /* Tests for arrays
    /**********************************************************************
     */

    public void testByteArray() throws Exception {
        byte[] result = JSON.std.beanFrom(byte[].class, quote("YWJj"));
        assertEquals("abc", new String(result, "UTF-8"));
    }

    public void testCharArray() throws Exception {
        char[] result = JSON.std.beanFrom(char[].class, quote("abc"));
        assertEquals("abc", new String(result));
    }

    public void testSimpleArray() throws Exception
    {
        _testArray("[true,\"abc\",3]", 3);
    }

    public void testEmptyArray() throws Exception
    {
        _testArray("[]", 0);
    }

    // separate tests since code path differs
    public void testSingleElementArray() throws Exception {
        _testArray("[12]", 1);
    }

    public void testSmallArray() throws Exception {
        _testArray("[true,42,\"maybe\"]", 3);
    }

    private void _testArray(String input, int expCount) throws Exception
    {
        Object ob;

        // first: can explicitly request an array:
        ob = JSON.std.arrayFrom(input);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));

        // via parser, too
        JsonParser p = parserFor(input);
        ob = JSON.std.arrayFrom(p);
        assertTrue(ob instanceof Object[]);
        assertEquals(expCount, ((Object[]) ob).length);
        assertEquals(input, JSON.std.asString(ob));
        p.close();

        // or, with "List of Any"
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
    /* Tests for Scalars
    /**********************************************************************
     */

    public void testBoolean() throws Exception {
        assertEquals(Boolean.TRUE, JSON.std.beanFrom(Boolean.class, "true"));
        BooleanWrapper w = JSON.std.beanFrom(BooleanWrapper.class, "{\"value\":true}");
        assertTrue(w.value);
    }

    public void testCharacter() throws Exception {
        assertEquals(Character.valueOf('a'), JSON.std.beanFrom(Character.class, "\"a\""));
    }

    public void testNumbers() throws Exception {
        assertEquals(Byte.valueOf((byte) 13), JSON.std.beanFrom(Byte.class, "13"));
        assertEquals(Short.valueOf((short) 13), JSON.std.beanFrom(Short.class, "13"));
        assertEquals(Long.valueOf(42L), JSON.std.beanFrom(Long.class, "42"));

        assertEquals(new BigDecimal("10.25"), JSON.std.beanFrom(BigDecimal.class, "10.25"));
        assertEquals(BigInteger.TEN, JSON.std.beanFrom(BigInteger.class, "10"));
        
        assertEquals(0.25, JSON.std.beanFrom(Double.class, "0.25"));
        assertEquals(0.25f, JSON.std.beanFrom(Float.class, "0.25"));
    }

    public void testMiscScalars() throws Exception {
        assertEquals(new Date(123456L), JSON.std.beanFrom(Date.class,"123456"));
        assertEquals(Object.class, JSON.std.beanFrom(Class.class, quote(Object.class.getName())));
    }
    
    /*
    /**********************************************************************
    /* Failing tests (mostly for code coverage)
    /**********************************************************************
     */

    public void testTreeReadWithoutCodec() throws Exception
    {
        try {
            JSON.std.treeFrom("{\"value\":[ 3 ]}");
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "not have configured `TreeCodec` to read `TreeNode`");
        }

        try {
            JSON.std.treeSequenceFrom("{\"value\":[ 3 ]}");
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "not have configured `TreeCodec` to read `TreeNode` sequence");
        }

        try {
            JSON.std.beanFrom(TreeNode.class, quote("abc"));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "No `TreeCodec` specified");
        }

        try {
            JSON.std.beanFrom(TreeWrapper.class, "{\"value\":[ 3 ]}");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "No `TreeCodec` specified");
        }
    }
        
    public void testTreeNodeCreationWithoutCodec() throws Exception {
        try {
            JSON.std.createArrayNode();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not have configured `TreeCodec` to create Array node");
        }

        try {
            JSON.std.createObjectNode();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not have configured `TreeCodec` to create Object node");
        }
    }

    // not yet supported (but probably should)
    public void testIntArray() throws Exception {
        try {
            JSON.std.beanFrom(IntArrayWrapper.class, "{\"value\":[ 3 ]}");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "not yet implemented");
        }
    }

    public void testInvalidSource() throws Exception {
        try {
            JSON.std.beanFrom(Object.class, Long.valueOf(67));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Can not use Source of type `java.lang.Long`");
        }
    }

    public void testEmptySource() throws Exception {
        try {
            JSON.std.beanFrom(Object.class, "   ");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "No content to map due to end-of-input");
        }
    }
    
    /*
    /**********************************************************************
    /* Other tests
    /**********************************************************************
     */

    public void testSimpleMixed() throws Exception
    {
        final String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
        _verifySimpleMixed(JSON.std.anyFrom(INPUT), INPUT);
        JsonParser p = parserFor(INPUT);
        _verifySimpleMixed(JSON.std.anyFrom(p), INPUT);
        p.close();
    }

    private void _verifySimpleMixed(Object ob, String json) throws Exception
    {
        assertTrue(ob instanceof Map);
        assertEquals(2, ((Map<?,?>) ob).size());
        Object list = (((Map<?,?>) ob).get("a"));
        assertTrue(list instanceof List<?>);
        
        // actually, verify with write...
        assertEquals(json, JSON.std.asString(ob));
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
}
