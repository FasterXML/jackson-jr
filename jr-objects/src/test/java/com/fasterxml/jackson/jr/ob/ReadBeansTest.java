package com.fasterxml.jackson.jr.ob;

import com.fasterxml.jackson.jr.ob.JSON;
//import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class ReadBeansTest extends TestBase
{
    static class TestBean {
        protected int x;
        protected String name;
        
        public void setName(String n) { name = n; }
        public void setX(int x) { this.x = x; }

        public int getX() { return x; }
        public String getName() { return name; }
    }

    public void testSimpleList() throws Exception
    {
        final String INPUT = aposToQuotes("{'name':'Bob','x':13}");
        TestBean bean = JSON.std.beanFrom(INPUT, TestBean.class);

        assertNotNull(bean);
        assertEquals("Bob", bean.name);
        assertEquals(13, bean.x);
    }
}
