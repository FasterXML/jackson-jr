package com.fasterxml.jackson.jr.ob;

import java.util.List;

import com.fasterxml.jackson.jr.ob.JSON;

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

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */
    
    public void testSimpleBean() throws Exception
    {
        final String INPUT = aposToQuotes("{'name':{'first':'Bob','last':'Burger'},'x':13}");
        TestBean bean = JSON.std.beanFrom(TestBean.class, INPUT);

        assertNotNull(bean);
        assertEquals(13, bean.x);
        assertNotNull(bean.name);
        assertEquals("Bob", bean.name.first);
        assertEquals("Burger", bean.name.last);
    }

    public void testSimpleBeanCollections() throws Exception
    {
        final String INPUT = aposToQuotes("["
                +"{'name':{'first':'Bob','last':'Burger'},'x':13}"
                +",{'x':-145,'name':{'first':'Billy','last':'Bacon'}}"
                +"]");

        // First, bean array
        TestBean[] beans = JSON.std.arrayOfFrom(TestBean.class, INPUT);
        assertNotNull(beans);
        assertEquals(2, beans.length);
        assertEquals(13, beans[0].x);
        assertEquals("Bob", beans[0].name.first);
        assertEquals("Burger", beans[0].name.last);
        assertEquals(-145, beans[1].x);
        assertEquals("Billy", beans[1].name.first);
        assertEquals("Bacon", beans[1].name.last);

        // then List
        List<TestBean> beans2 = JSON.std.listOfFrom(TestBean.class, INPUT);
        assertNotNull(beans2);
        assertEquals(2, beans2.size());
        assertEquals(13, beans2.get(0).x);
        assertEquals("Bob", beans2.get(0).name.first);
        assertEquals("Burger", beans2.get(0).name.last);
        assertEquals(-145, beans2.get(1).x);
        assertEquals("Billy", beans2.get(1).name.first);
        assertEquals("Bacon", beans2.get(1).name.last);
    }
}
