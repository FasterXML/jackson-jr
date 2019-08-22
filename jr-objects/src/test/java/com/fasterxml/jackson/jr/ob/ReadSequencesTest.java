package com.fasterxml.jackson.jr.ob;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParser;

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
        _verifyAnySequence(it);
        it.close();

        // and then parser we create
        JsonParser p = JSON.std.createParser(new ByteArrayInputStream(INPUT.getBytes("UTF-8")));

        it = JSON.std.anySequenceFrom(p);
        _verifyAnySequence(it);
        it.close();
        p.close();
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
}
