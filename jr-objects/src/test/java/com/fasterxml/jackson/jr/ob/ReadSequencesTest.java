package com.fasterxml.jackson.jr.ob;

import java.io.*;
import java.util.*;

import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.StreamReadException;

public class ReadSequencesTest extends TestBase
{
    static class Bean {
        public int id;
        public String msg;
    }

    /*
    /**********************************************************************
    /* Tests for "Simple" content
    /**********************************************************************
     */

    public void testAnySequence() throws Exception
    {
        final String INPUT = aposToQuotes("'hello world!' 127 true [ 1, 2, 3]\nnull { 'msg':'none'}   ");

        // First, managed
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        assertNotNull(it.parser());
        assertNotNull(it.currentLocation());

        _verifyAnySequence(it);
        it.close();
        assertFalse(it.hasNext());
        assertFalse(it.hasNextValue());

        // and then parser we create
        JsonParser p = JSON.std.createParser(new ByteArrayInputStream(INPUT.getBytes("UTF-8")));

        it = JSON.std.anySequenceFrom(p);
        _verifyAnySequence(it);
        it.close();
        p.close();

        // Plus should not hurt to try close()ing more (no effect either)
        it.close();
        it.close();

        // and finally, test early close() top
        it = JSON.std.anySequenceFrom(INPUT);
        assertNotNull(it.next());
        it.close();
    }

    public void testAnyViaAll() throws Exception
    {
        final String INPUT = "1\n3\n3";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        List<Object> stuff = it.readAll();
        it.close();
        final List<Integer> exp = Arrays.asList(Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(3));
        assertEquals(exp, stuff);

        // and also alternative
        Collection<Object> result = new HashSet<Object>();
        result.add(Integer.valueOf(42));
        result.add(Integer.valueOf(28));
        it = JSON.std.anySequenceFrom(INPUT);
        Collection<Object> result2 = it.readAll(result);
        assertEquals(4, result2.size());
        assertTrue(result2.contains(Integer.valueOf(28)));
        assertTrue(result2.contains(Integer.valueOf(42)));
        assertTrue(result2.contains(Integer.valueOf(1)));
        assertTrue(result2.contains(Integer.valueOf(3)));
    }

    private void _verifyAnySequence(ValueIterator<Object> it) throws Exception
    {
        assertTrue(it.hasNext());
        assertEquals("hello world!", it.nextValue());

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(127), it.nextValue());

        assertTrue(it.hasNext());
        assertEquals(Boolean.TRUE, it.nextValue());

        assertTrue(it.hasNext());
        assertEquals(Arrays.asList(1, 2, 3), it.nextValue());

        assertTrue(it.hasNext());
        assertNull(it.nextValue());

        assertTrue(it.hasNext());
        assertEquals(Collections.singletonMap("msg",  "none"), it.nextValue());

        assertFalse(it.hasNext());
    }

    /*
    /**********************************************************************
    /* Tests for POJOs
    /**********************************************************************
     */

    public void testBeanSequence() throws Exception
    {
        final String INPUT = aposToQuotes("{'id':1, 'msg':'foo'} {'id':2, 'msg':'Same'} null   ");

        // First, managed
        ValueIterator<Bean> it = JSON.std.beanSequenceFrom(Bean.class, INPUT);
        _verifyBeanSequence(it);
        it.close();

        // and parser we create
        JsonParser p = JSON.std.createParser(new StringReader(INPUT));

        it = JSON.std.beanSequenceFrom(Bean.class, p);
        _verifyBeanSequence(it);
        it.close();
        p.close();
    }

    private void _verifyBeanSequence(ValueIterator<Bean> it) throws Exception
    {
        assertTrue(it.hasNext());
        Bean bean = it.nextValue();
        assertEquals(1, bean.id);
        assertEquals("foo", bean.msg);

        assertTrue(it.hasNext());
        bean = it.nextValue();
        assertEquals(2, bean.id);
        assertEquals("Same", bean.msg);

        assertTrue(it.hasNext());
        assertNull(it.nextValue());

        assertFalse(it.hasNext());
    }

    /*
    /**********************************************************************
    /* See if resync might work...
    /**********************************************************************
     */

    public void testResync() throws Exception
    {
        final String INPUT = "1\n[ 300a ]\n3";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.nextValue());

        // then malformed value
        try {
            it.nextValue();
            fail("Should not pass");
        } catch (StreamReadException e) {
            verifyException(e, "Unexpected character ('a'");
        }

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.nextValue());

        assertFalse(it.hasNext());
        it.close();
    }

    /*
    /**********************************************************************
    /* Tests for illegal/invalid operation handling
    /**********************************************************************
     */

    public void testBrokenContent() throws Exception
    {
        final String INPUT = "1  abc";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.nextValue());

        try {
            it.hasNext();
            fail("Should not pass");
        } catch (StreamReadException e) {
            verifyException(e, "Unrecognized token");
        }
    }

    public void testReadAfterEnd() throws Exception
    {
        final String INPUT = "1\n3\n3";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        it.readAll();
        try {
            it.nextValue();
            fail("Should not pass");
        } catch (NoSuchElementException e) {
            ; // expected, no message
        }
    }

    public void testReadAfterClose() throws Exception
    {
        final String INPUT = "1\n3\n3";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        it.nextValue();
        it.close();
        try {
            it.nextValue();
            fail("Should not pass");
        } catch (NoSuchElementException e) {
            ; // expected, no message
        }
    }
    
    public void testTryToRemove() throws Exception
    {
        final String INPUT = "1\n3\n3";
        ValueIterator<Object> it = JSON.std.anySequenceFrom(INPUT);
        try {
            it.remove();
            fail("Should not pass");
        } catch (UnsupportedOperationException e) {
            ; // expected, no message
        }
    }
}
