package com.fasterxml.jackson.jr.ob;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

import static org.junit.jupiter.api.Assertions.*;

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

    static class DateWrapper {
        public Date value;
    }

    /*
    /**********************************************************************
    /* Tests for arrays
    /**********************************************************************
     */

    @Test
    public void testByteArray() throws Exception {
        byte[] result = JSON.std.beanFrom(byte[].class, q("YWJj"));
        assertEquals("abc", new String(result, "UTF-8"));
    }

    @Test
    public void testCharArray() throws Exception {
        char[] result = JSON.std.beanFrom(char[].class, q("abc"));
        assertEquals("abc", new String(result));
    }

    @Test
    public void testSimpleArray() throws Exception
    {
        _testArray("[true,\"abc\",3]", 3);
    }

    @Test
    public void testEmptyArray() throws Exception
    {
        _testArray("[]", 0);
    }

    // separate tests since code path differs
    @Test
    public void testSingleElementArray() throws Exception {
        _testArray("[12]", 1);
    }

    @Test
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

    @Test
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

    @Test
    public void testBoolean() throws Exception {
        assertEquals(Boolean.TRUE, JSON.std.beanFrom(Boolean.class, "true"));
        BooleanWrapper w = JSON.std.beanFrom(BooleanWrapper.class, "{\"value\":true}");
        assertTrue(w.value);
    }

    @Test
    public void testCharacter() throws Exception {
        assertEquals(Character.valueOf('a'), JSON.std.beanFrom(Character.class, "\"a\""));
    }

    @Test
    public void testNumbers() throws Exception {
        assertEquals(Byte.valueOf((byte) 13), JSON.std.beanFrom(Byte.class, "13"));
        assertEquals(Short.valueOf((short) 13), JSON.std.beanFrom(Short.class, "13"));
        assertEquals(Long.valueOf(42L), JSON.std.beanFrom(Long.class, "42"));

        assertEquals(new BigDecimal("10.25"), JSON.std.beanFrom(BigDecimal.class, "10.25"));
        assertEquals(BigInteger.TEN, JSON.std.beanFrom(BigInteger.class, "10"));
        
        assertEquals(0.25, JSON.std.beanFrom(Double.class, "0.25"));
        assertEquals(0.25f, JSON.std.beanFrom(Float.class, "0.25"));
    }

    // 07-Jul-2020, tatu: Should probably make fail, but doesn't yet:
    /*
    @Test
    public void testIntegerFail() throws Exception {
        try {
            Integer I = JSON.std.beanFrom(Integer.class, "true");
            fail("Should not pass, got: "+I);
        } catch (JSONObjectException e) {
            verifyException(e, "Can not get long numeric");
        }
    }
    */

    @Test
    public void testBooleanFail() throws Exception {
        try {
            Boolean B = JSON.std.beanFrom(Boolean.class, "13");
            fail("Should not pass, got: "+B);
        } catch (JSONObjectException e) {
            verifyException(e, "Can not create a `java.lang.Boolean` instance out of JSON Number");
        }
    }

    @Test
    public void testMiscScalars() throws Exception {
        assertEquals(new Date(123456L), JSON.std.beanFrom(Date.class,"123456"));
        assertEquals(Object.class, JSON.std.beanFrom(Class.class, q(Object.class.getName())));
    }

    @Test
    public void testMiscUriTypes() throws Exception
    {
        final String URL_STR = "http://fasterxml.com";
        final URL url = new URL(URL_STR);
        assertEquals(url, JSON.std.beanFrom(URL.class, q(URL_STR)));

        Path p = Paths.get(new URI("file:///foo/bar.txt"));
        String json = JSON.std.asString(p);
        assertEquals(p,
                JSON.std.beanFrom(Path.class, json));
    }

    @Test
    public void testMiscScalarFail() throws Exception {
        for (String input : new String[] { " false ",  "true", "[ ]", "{ }" } ) {
            try {
                JSON.std.beanFrom(Date.class, input);
                fail("Should not pass");
            } catch (JSONObjectException e) {
                verifyException(e, "Can not get long numeric");
            }
        }
    }

    /*
    /**********************************************************************
    /* Tests for Scalars, null handling
    /**********************************************************************
     */

    @Test
    public void testNullForMiscNumbers() throws Exception {
        assertNull(JSON.std.beanFrom(Integer.class," null "));
        assertNull(JSON.std.beanFrom(Long.class," null "));
        assertNull(JSON.std.beanFrom(Double.class," null "));

        assertNull(JSON.std.beanFrom(BigInteger.class," null "));
        assertNull(JSON.std.beanFrom(BigDecimal.class," null "));
    }

    @Test
    public void testNullForMiscScalars() throws Exception {
        assertNull(JSON.std.beanFrom(Date.class," null "));
        assertNull(JSON.std.beanFrom(Calendar.class," null "));

        assertNull(JSON.std.beanFrom(String.class," null "));
        assertNull(JSON.std.beanFrom(Class.class," null "));
        assertNull(JSON.std.beanFrom(File.class," null "));
        assertNull(JSON.std.beanFrom(URL.class," null "));
        assertNull(JSON.std.beanFrom(URI.class," null "));
    }

    // Testing that `null` will not cause an exception, for now at least
    @Test
    public void testNullForPrimitiveProperties() throws Exception {
        BooleanWrapper w = JSON.std.beanFrom(BooleanWrapper.class, a2q("{'value':null}"));
        assertNotNull(w);
        assertFalse(w.value);
    }

    @Test
    public void testNullForScalarProperties() throws Exception {
        DateWrapper w = JSON.std.beanFrom(DateWrapper.class, a2q("{'value':null}"));
        assertNotNull(w);
        assertNull(w.value);
    }

    /*
    /**********************************************************************
    /* Tests for other simple types
    /**********************************************************************
     */

    @Test
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

    @Test
    public void testSimpleEnums() throws Exception
    {
        // First using index
        ABC abc = JSON.std.beanFrom(ABC.class, String.valueOf(ABC.B.ordinal()));
        assertEquals(ABC.B, abc);

        // then from name
        abc = JSON.std.beanFrom(ABC.class, q("C"));
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

    /*
    /**********************************************************************
    /* Failing tests (mostly for code coverage)
    /**********************************************************************
     */

    @Test
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
            JSON.std.beanFrom(TreeNode.class, q("abc"));
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

    @Test
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

    @Test
    public void testInvalidSource() throws Exception {
        try {
            JSON.std.beanFrom(Object.class, Long.valueOf(67));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Can not use Source of type `java.lang.Long`");
        }
    }

    @Test
    public void testEmptySource() throws Exception {
        try {
            JSON.std.beanFrom(Object.class, "   ");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "No content to map due to end-of-input");
        }
    }
}
