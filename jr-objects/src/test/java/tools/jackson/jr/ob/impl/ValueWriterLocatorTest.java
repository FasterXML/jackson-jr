package tools.jackson.jr.ob.impl;

import java.util.*;

import tools.jackson.jr.ob.TestBase;

public class ValueWriterLocatorTest extends TestBase
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
        // note: must create non-blue-print instance to avoid NPE
        ValueWriterLocator td = ValueWriterLocator.blueprint(null, null)
                .perOperationInstance(null, 0);
        assertEquals(ValueWriterLocator.SER_STRING, td.findSerializationType(String.class));
        assertEquals(ValueWriterLocator.SER_CHAR_ARRAY, td.findSerializationType(char[].class));
        assertEquals(ValueWriterLocator.SER_INT_ARRAY, td.findSerializationType(int[].class));
        assertEquals(ValueWriterLocator.SER_LONG_ARRAY, td.findSerializationType(long[].class));
        assertEquals(ValueWriterLocator.SER_BOOLEAN_ARRAY, td.findSerializationType(boolean[].class));
        assertEquals(ValueWriterLocator.SER_OBJECT_ARRAY, td.findSerializationType(Object[].class));
        assertEquals(ValueWriterLocator.SER_CHARACTER_SEQUENCE, td.findSerializationType(StringBuffer.class));
        assertEquals(ValueWriterLocator.SER_COLLECTION, td.findSerializationType(LinkedHashSet.class));
        assertEquals(ValueWriterLocator.SER_LIST, td.findSerializationType(ArrayList.class));

        assertEquals(ValueWriterLocator.SER_NUMBER_INTEGER, td.findSerializationType(Integer.TYPE));
        assertEquals(ValueWriterLocator.SER_NUMBER_INTEGER_WRAPPER, td.findSerializationType(Integer.class));

        assertEquals(ValueWriterLocator.SER_NUMBER_LONG, td.findSerializationType(Long.TYPE));
        assertEquals(ValueWriterLocator.SER_NUMBER_LONG_WRAPPER, td.findSerializationType(Long.class));
        
        assertEquals(ValueWriterLocator.SER_NUMBER_DOUBLE, td.findSerializationType(Double.TYPE));
        assertEquals(ValueWriterLocator.SER_NUMBER_DOUBLE_WRAPPER, td.findSerializationType(Double.class));

        // more specific types
        assertEquals(ValueWriterLocator.SER_CALENDAR, td.findSerializationType(Calendar.class));
        assertEquals(ValueWriterLocator.SER_CALENDAR, td.findSerializationType(GregorianCalendar.class));
        assertEquals(ValueWriterLocator.SER_DATE, td.findSerializationType(new GregorianCalendar().getTime().getClass()));
        assertEquals(ValueWriterLocator.SER_UUID, td.findSerializationType(UUID.class));
    }
}
