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

    // // Test types from 'jvm-serializers' for bit more complete test

    public static class MediaItem
    {
         public Media media;
         public List<Image> images;

         public MediaItem addPhoto(Image i) {
             if (images == null) {
                 images = new ArrayList<Image>();
             }
             images.add(i);
             return this;
         }

         public Media getMedia() { return media; }
         public void setMedia(Media m) { media = m; }

         public List<Image> getImages() { return images; }
         public void setImages(List<Image> i) { images = i; }
    }

    public enum Size { SMALL, LARGE };
    
    public static class Image
    {
        public Image() { }
        public Image(String uri, String title, int w, int h, Size s) {
            this.uri = uri;
            this.title = title;
            width = w;
            height = h;
            size = s;
        }

        public String uri;
        public String title;
        public int width, height;
        public Size size;    

        public String getUri() { return uri; }
        public String getTitle() { return title; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public Size getSize() { return size; }

        public void setUri(String v) { uri = v; }
        public void setTitle(String v) { title = v; }
        public void setWidth(int v) { width = v; }
        public void setHeight(int v) { height = v; }
        public void setSize(Size v) { size = v; }
    } 
    
    public enum Player { JAVA, FLASH; }

    public static class Media {

        public String uri;
        public String title;
        public int width;
        public int height;
        public String format;
        public long duration;
        public long size;
        public int bitrate;

        public List<String> persons;
        
        public Player player;
        public String copyright;

        public Media addPerson(String p) {
            if (persons == null) {
                persons = new ArrayList<String>();
            }
            persons.add(p);
            return this;
        }

        public String getUri() { return uri; }
        public String getTitle() { return title; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }

        public String getFormat() { return format; }
        public long getDuration() { return duration; }
        public long getSize() { return size; }
        public int getBitrate() { return bitrate; }

        public List<String> getPersons() { return persons; }
        public Player getPlayer() { return player; }
        public String getCopyright() { return copyright; }
        
        public void setUri(String v) { uri = v; }
        public void setTitle(String v) { title = v; }
        public void setWidth(int v) { width = v; }
        public void setHeight(int v) { height = v; }

        public void setFormat(String v) { format = v; }
        public void setDuration(long v) { duration = v; }
        public void setSize(long v) { size = v; }
        public void setBitrate(int v) { bitrate = v; }

        public void setPersons(List<String> v) { persons = v; }
        public void setPlayer(Player v) { player = v; }
        public void setCopyright(String v) { copyright = v; }
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
        Media content = new Media();
        content.player = Player.JAVA;
        content.uri = "http://javaone.com/keynote.mpg";
        content.title = "Javaone Keynote";
        content.width = 640;
        content.height = 480;
        content.format = "video/mpeg4";
        content.duration = 18000000L;
        content.size = 58982400L;
        content.bitrate = 262144;
        content.copyright = "None";
        content.addPerson("Bill Gates");
        content.addPerson("Steve Jobs");

        MediaItem input = new MediaItem();
        input.media = content;

        input.addPhoto(new Image("http://javaone.com/keynote_large.jpg", "Javaone Keynote", 1024, 768, Size.LARGE));
        input.addPhoto(new Image("http://javaone.com/keynote_small.jpg", "Javaone Keynote", 320, 240, Size.SMALL));

        String json = JSON.std.asString(input);

        MediaItem result = JSON.std
            .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY)
            .beanFrom(MediaItem.class, json);

        assertNotNull(result);
        assertEquals(262144, result.media.bitrate);
        assertEquals("Steve Jobs", result.media.persons.get(1));
    }
}
