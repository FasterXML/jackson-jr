package com.fasterxml.jackson.jr.ob;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class WriteBeansTest extends TestBase
{
    static class TestBean {
        public int getX() { return 1; }
        public void setX(int x) { }

        public int getY() { return 3; }
    }

    static class BinaryBean {
        protected final static byte[] STUFF = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        
        public byte[] getStuff() {
            return STUFF;
        }
    }

    public void testBinary() throws Exception
    {
        String json = JSON.std.asString(new BinaryBean());
        Map<String,Object> map = JSON.std.mapFrom(json);
        if (1 != map.size()) {
            fail("Wrong Map contents: "+json);
        }
        String base64 = (String) map.get("stuff");
        assertNotNull(base64);
        assertEquals("AQIDBAUGBw==", base64);
    }
    
    public void testSimpleMap() throws Exception
    {
        // first: with default settings, should serialize 2 props
        String json = JSON.std.asString(new TestBean());
        
        Map<String,Object> map = JSON.std.mapFrom(json);
        if ((2 != map.size())
                || !Integer.valueOf(1).equals(map.get("x"))
                || !Integer.valueOf(3).equals(map.get("y"))
                ){
            fail("Wrong Map contents (expected 'x' and 'y' for JSON: "+json);
        }

        // and then different configs
        json = JSON.std.without(Feature.WRITE_READONLY_BEAN_PROPERTIES)
                .asString(new TestBean());
        map = JSON.std.mapFrom(json);
        if ((1 != map.size())
                || !Integer.valueOf(1).equals(map.get("x"))
                ){
            fail("Wrong Map contents (expected just 'x' for JSON: "+json);
        }
    }
    
}
