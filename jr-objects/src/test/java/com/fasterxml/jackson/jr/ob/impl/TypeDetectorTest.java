package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

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

    public void testBasicTypeDetectionForSer() {
        TypeDetector td = TypeDetector.blueprint(JSON.std.getStreamingFactory(),
                JSON.Feature.defaults());
        assertEquals(TypeDetector.SER_STRING, td.findSerializationType(String.class));
        assertEquals(TypeDetector.SER_CHAR_ARRAY, td.findSerializationType(char[].class));
        assertEquals(TypeDetector.SER_INT_ARRAY, td.findSerializationType(int[].class));
        assertEquals(TypeDetector.SER_LONG_ARRAY, td.findSerializationType(long[].class));
        assertEquals(TypeDetector.SER_BOOLEAN_ARRAY, td.findSerializationType(boolean[].class));
        assertEquals(TypeDetector.SER_OBJECT_ARRAY, td.findSerializationType(Object[].class));
        assertEquals(TypeDetector.SER_CHARACTER_SEQUENCE, td.findSerializationType(StringBuffer.class));
        assertEquals(TypeDetector.SER_COLLECTION, td.findSerializationType(LinkedHashSet.class));
        assertEquals(TypeDetector.SER_LIST, td.findSerializationType(ArrayList.class));

        assertEquals(TypeDetector.SER_NUMBER_INTEGER, td.findSerializationType(Integer.class));
        assertEquals(TypeDetector.SER_NUMBER_INTEGER, td.findSerializationType(Integer.TYPE));
        
        // more specific types
        assertEquals(TypeDetector.SER_CALENDAR, td.findSerializationType(Calendar.class));
        assertEquals(TypeDetector.SER_CALENDAR, td.findSerializationType(GregorianCalendar.class));
        assertEquals(TypeDetector.SER_DATE, td.findSerializationType(new GregorianCalendar().getTime().getClass()));
        assertEquals(TypeDetector.SER_UUID, td.findSerializationType(UUID.class));
    }
}
