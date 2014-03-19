package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.jr.ob.*;

public class TypeDetectorTest extends TestBase
{
    static class TestBean {
        public int getX() { return 1; }
        public void setX(int x) { }

        public String getName() { return null; }

        public void setY(int y) { }

        public int bebop() { return 3; }
        public void zipdoo(int value) { }

        // not a regular getter (could be indexed)
        public void setEmUp(int index, String value) { }
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */
    
    public void testSimpleWithSerialization() 
    {
        TypeDetector td = TypeDetector.rootDetector(true, JSON.Feature.defaults());
        BeanDefinition def = td._resolveBean(TestBean.class);
        assertNotNull(def);

        List<BeanProperty> props = Arrays.asList(def._properties);
        assertEquals(3, props.size());
        Map<String, BeanProperty> map = new HashMap<String, BeanProperty>();
        for (BeanProperty prop : props) {
            map.put(prop._name.getValue(), prop);
        }

        BeanProperty prop;

        prop = map.get("x");
        assertNotNull(prop);
        assertNotNull(prop._getMethod);
        assertNotNull(prop._setMethod);
        prop = map.get("y");
        assertNotNull(prop);
        assertNull(prop._getMethod);
        assertNotNull(prop._setMethod);
        prop = map.get("name");
        assertNotNull(prop);
        assertNotNull(prop._getMethod);
        assertNull(prop._setMethod);
    }

    public void testBasicTypeDetectionForSer() {
        _verifyBasicTypes(true);
    }

    public void testBasicTypeDetectionForDeser() {
        _verifyBasicTypes(false);
    }

    private void _verifyBasicTypes(boolean forSerialization)
    {
        TypeDetector td = TypeDetector.rootDetector(forSerialization, JSON.Feature.defaults());
        assertEquals(TypeDetector.SER_STRING, td.findFullType(String.class));
        assertEquals(TypeDetector.SER_CHAR_ARRAY, td.findFullType(char[].class));
        assertEquals(TypeDetector.SER_INT_ARRAY, td.findFullType(int[].class));
        assertEquals(TypeDetector.SER_LONG_ARRAY, td.findFullType(long[].class));
        assertEquals(TypeDetector.SER_BOOLEAN_ARRAY, td.findFullType(boolean[].class));
        assertEquals(TypeDetector.SER_OBJECT_ARRAY, td.findFullType(Object[].class));
        assertEquals(TypeDetector.SER_CHARACTER_SEQUENCE, td.findFullType(StringBuffer.class));
        assertEquals(TypeDetector.SER_COLLECTION, td.findFullType(LinkedHashSet.class));
        assertEquals(TypeDetector.SER_LIST, td.findFullType(ArrayList.class));

        assertEquals(TypeDetector.SER_NUMBER_INTEGER, td.findFullType(Integer.class));
        assertEquals(TypeDetector.SER_NUMBER_INTEGER, td.findFullType(Integer.TYPE));
        
        // more specific types
        assertEquals(TypeDetector.SER_CALENDAR, td.findFullType(Calendar.class));
        assertEquals(TypeDetector.SER_CALENDAR, td.findFullType(GregorianCalendar.class));
        assertEquals(TypeDetector.SER_DATE, td.findFullType(new GregorianCalendar().getTime().getClass()));
        assertEquals(TypeDetector.SER_UUID, td.findFullType(UUID.class));
    }
}
