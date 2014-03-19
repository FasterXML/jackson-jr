package com.fasterxml.jackson.jr.ob;

import com.fasterxml.jackson.jr.ob.JSON;
//import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class ReadBeansTest extends TestBase
{
    static class TestBean {
        protected int x;
        protected NameBean name;
        
        public void setName(NameBean n) { name = n; }
        public void setX(int x) { this.x = x; }

        public int getX() { return x; }
        public NameBean getName() { return name; }
    }

    static class NameBean {
        protected String first, last;
        
        public String getFirst() { return first; }
        public String getLast() { return last; }

        public void setFirst(String n) { first = n; }
        public void setLast(String n) { last = n; }
    }

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
    
    public void testSimpleList() throws Exception
    {
        final String INPUT = aposToQuotes("{'name':{'first':'Bob','last':'Burger'},'x':13}");
        TestBean bean = JSON.std.beanFrom(INPUT, TestBean.class);

        assertNotNull(bean);
        assertEquals(13, bean.x);
        assertNotNull(bean.name);
        assertEquals("Bob", bean.name.first);
        assertEquals("Burger", bean.name.last);
    }

    public void testStringCtor() throws Exception
    {
        FromString output = JSON.std.beanFrom(quote("abc"), FromString.class);
        assertNotNull(output);
        assertEquals("abc", output.value);
    }

    public void testLongCtor() throws Exception
    {
        FromLong1 output = JSON.std.beanFrom("123", FromLong1.class);
        assertNotNull(output);
        assertEquals(123L, output.value);

        FromLong2 output2 = JSON.std.beanFrom("456", FromLong2.class);
        assertNotNull(output2);
        assertEquals(456L, output2.value);
    }
}
