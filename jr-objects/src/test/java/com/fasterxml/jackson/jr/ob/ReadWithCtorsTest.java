package com.fasterxml.jackson.jr.ob;

import com.fasterxml.jackson.jr.ob.JSON;

public class ReadWithCtorsTest extends TestBase
{
    static class FromString {
        protected String value;
        public FromString(String v) { value = v; }
    }

    static class FromLong1 {
        protected long value;
        public FromLong1(long v) { value = v; }
    }

    static class FromLong2 {
        protected long value;
        public FromLong2(Long v) { value = v.longValue(); }
    }
    
    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    public void testStringCtor() throws Exception
    {
        FromString output = JSON.std.beanFrom(FromString.class, quote("abc"));
        assertNotNull(output);
        assertEquals("abc", output.value);
    }

    public void testLongCtor() throws Exception
    {
        FromLong1 output = JSON.std.beanFrom(FromLong1.class, "123");
        assertNotNull(output);
        assertEquals(123L, output.value);

        FromLong2 output2 = JSON.std.beanFrom(FromLong2.class, "456");
        assertNotNull(output2);
        assertEquals(456L, output2.value);
    }
}
