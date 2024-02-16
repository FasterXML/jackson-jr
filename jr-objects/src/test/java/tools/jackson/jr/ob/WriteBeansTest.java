package tools.jackson.jr.ob;

import java.util.*;

import tools.jackson.jr.ob.JSON.Feature;

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

    static class BeanBase {
        int _value;

        public int getValue() { return _value; }
        public void setValue(int v) { _value = v; }
    }

    static class BaseImpl extends BeanBase {
        int _extra;

        protected BaseImpl() { }
        public BaseImpl(int v, int x) {
            _value = v;
            _extra = x;
        }

        public int getExtra() { return _extra; }
        public void setExtra(int v) { _extra = v; }
        
    }

    static class StringBean {
        public String value;

        public StringBean(String v) { value = v; }
    }

    static class StringBeanBean {
        public StringBean bean;

        public StringBeanBean(StringBean b) {
            bean = b;
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

    // Make sure we handle stuff from base class too
    public void testMethodsFromSuperclass() throws Exception
    {
        String json = JSON.std.asString(new BaseImpl(1, 2));
        Map<String,Object> map = JSON.std.mapFrom(json);
        if ((2 != map.size())
                || !Integer.valueOf(1).equals(map.get("value"))
                || !Integer.valueOf(2).equals(map.get("extra"))
                ){
            fail("Wrong Map contents (expected 'value' and 'extra' for JSON: "+json);
        }

        BaseImpl result = JSON.std.beanFrom(BaseImpl.class,
                a2q("{ 'extra':5, 'value':-245 }"));
        assertEquals(5, result.getExtra());
        assertEquals(-245, result.getValue());
    }

    public void testBeanNulls() throws Exception
    {
        final JSON withNulls = JSON.std.with(JSON.Feature.WRITE_NULL_PROPERTIES);
        
        // by default, no nulls for either "String" property
        assertEquals("{}", JSON.std.asString(new StringBean(null)));
        assertEquals("{}", JSON.std.asString(new StringBeanBean(null)));
        assertEquals(a2q("{'bean':{}}"),
                JSON.std.asString(new StringBeanBean(new StringBean(null))));

        // but we can make them appear
        assertEquals(a2q("{'value':null}"),
                withNulls.asString(new StringBean(null)));
        assertEquals(a2q("{'bean':null}"),
                withNulls.asString(new StringBeanBean(null)));
        assertEquals(a2q("{'bean':{'value':null}}"),
                withNulls.asString(new StringBeanBean(new StringBean(null))));
    }
}
