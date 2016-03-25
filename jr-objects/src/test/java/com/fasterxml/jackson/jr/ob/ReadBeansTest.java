package com.fasterxml.jackson.jr.ob;

import java.util.*;

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

    static class MapBean {
        protected Map<String,Integer> stuff;
        
        public Map<String,Integer> getStuff() { return stuff; }
        public void setStuff(Map<String,Integer> s) { stuff = s; }
    }

    static class NameListBean {
        protected List<NameBean> names;
        
        public List<NameBean> getNames() { return names; }
        public void setNames(List<NameBean> n) { names = n; }
    }

    interface Bean<T> {
        public void setValue(T t);
    }
    
    static class LongBean implements Bean<Long> {
        Long value;

        @Override
        public void setValue(Long v) {
            value = v;
        }
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

    public void testUnknownProps() throws Exception
    {
        final String INPUT = aposToQuotes("{'first':'Bob','middle':'Eugene', 'last':'Smith'}");

        // First: fine if marked as such
        NameBean name = JSON.std
                .without(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                .beanFrom(NameBean.class, INPUT);
        assertNotNull(name);
        assertEquals("Bob", name.first);
        assertEquals("Smith", name.last);

        // but not if check enabled
        try {
            name = JSON.std
                    .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
                    .beanFrom(NameBean.class, INPUT);
            fail("Should have thrown exception");
        } catch (JSONObjectException e) {
            verifyException(e, "unrecognized JSON property 'middle'");
        }
    }

    public void testPOJOWithList() throws Exception
    {
        final String INPUT = aposToQuotes("{'names': [ { 'first':'John','last':'Smith' },"
                +"{'first':'Bob','last':'Burger' } ] }");
        NameListBean list = JSON.std.beanFrom(NameListBean.class, INPUT);
        assertNotNull(list);
        assertNotNull(list.names);
        assertEquals(2, list.names.size());
        NameBean name = list.names.get(1);
        assertEquals("Burger", name.getLast());
    }
    
    public void testPOJOWithMap() throws Exception
    {
        final String INPUT = aposToQuotes("{'stuff': { 'a':3, 'b':4 } }");
        MapBean map = JSON.std.beanFrom(MapBean.class, INPUT);
        assertNotNull(map);
        assertNotNull(map.stuff);
        assertEquals(2, map.stuff.size());
        assertEquals(Integer.valueOf(4), map.stuff.get("b"));
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

    public void testJvmSerializersPOJO() throws Exception
    {
        MediaItem.Content content = new MediaItem.Content();
        content.setPlayer(MediaItem.Player.JAVA);
        content.setUri("http://javaone.com/keynote.mpg");
        content.setTitle("Javaone Keynote");
        content.setWidth(640);
        content.setHeight(480);
        content.setFormat("video/mpeg4");
        content.setDuration(18000000L);
        content.setSize(58982400L);
        content.setBitrate(262144);
        content.setCopyright("None");
        content.addPerson("Bill Gates");
        content.addPerson("Steve Jobs");

        MediaItem input = new MediaItem(content);

        final String IMAGE_URI1 = "http://javaone.com/keynote_large.jpg";
        input.addPhoto(new MediaItem.Photo(IMAGE_URI1, "Javaone Keynote", 1024, 768, MediaItem.Size.LARGE));
        input.addPhoto(new MediaItem.Photo("http://javaone.com/keynote_small.jpg", "Javaone Keynote",
                320, 240, MediaItem.Size.SMALL));

        String json = JSON.std.asString(input);

        MediaItem result = JSON.std
            .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
            .beanFrom(MediaItem.class, json);

        assertNotNull(result);
        assertEquals(262144, result.getContent().getBitrate());
        assertEquals("Steve Jobs", result.getContent().getPersons().get(1));

        List<?> im = result.getImages();
        assertEquals(2, im.size());
        assertEquals(MediaItem.Photo.class, im.get(0).getClass());

        MediaItem.Photo im1 = (MediaItem.Photo) im.get(0);
        assertNotNull(im1);
        assertEquals(IMAGE_URI1, im1.getUri());
    }

    // For [#15]
    public void testLongBind() throws Exception
    {
        final String INPUT = "{\"value\":2}";
        LongBean bean = JSON.std.beanFrom(LongBean.class, INPUT);
        assertNotNull(bean);
        assertEquals(Long.valueOf(2L), bean.value);
    }

    public void testPojoArray() throws Exception
    {
        LongBean[] empty = JSON.std
                .with(JSON.Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS)
                .arrayOfFrom(LongBean.class, "[ ]");
        assertNotNull(empty);
        assertEquals(0, empty.length);

        LongBean[] result = JSON.std
                .with(JSON.Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS)
                .arrayOfFrom(LongBean.class, "[{\"value\":3}]");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(3L, result[0].value.longValue());
    }
}
