package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

import static org.junit.jupiter.api.Assertions.*;

public class CustomValueReadersTest extends TestBase
{
    static class CustomValue {
        public int value;

        // 2nd arg just to avoid discovery
        public CustomValue(int v, boolean b) {
            // and to ensure it goes through constructor, add 1
            value = v + 1;
        }
    }

    static class CustomValueBean {
        public CustomValue custom;

        protected CustomValueBean() { }
        public CustomValueBean(int v) {
            custom = new CustomValue(v, false);
        }
    }

    enum ABC {
        A, B, C, DEF;
    }
    
    static class CustomValueReader extends ValueReader {
        private final int delta;

        public CustomValueReader(int d) {
            super(CustomValue.class);
            delta = d;
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            return new CustomValue(p.getIntValue() + delta, true);
        }

        // Base class impl should be fine, although we'd use this for optimal
        /*
        @Override
        public Object readNext(JSONReader reader, JsonParser p) throws IOException {
            return new CustomValue(p.nextIntValue(-1), true);
        }
        */
    }

    static class ABCValueReader extends ValueReader {
        public ABCValueReader() {
            super(ABC.class);
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            final String str = p.getText();
            if ("n/a".equals(str)) {
                return ABC.DEF;
            }
            return ABC.valueOf(str);
        }
    }

    static class CapStringReader extends ValueReader {
        public CapStringReader() {
            super(String.class);
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            return p.getText().toUpperCase();
        }
    }

    static class OverrideStringReader extends ValueReader {
        final String _value;

        public OverrideStringReader(String str) {
            super(String.class);
            _value = str;
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            p.skipChildren();
            return _value;
        }
    }
    
    static class CustomReaders extends ReaderWriterProvider {
        final int delta;

        public CustomReaders(int d) {
            delta = d;
        }

        @Override
        public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
            if (type.equals(CustomValue.class)) {
                return new CustomValueReader(delta);
            } else if (type.equals(ABC.class)) {
                return new ABCValueReader();
            }
            return null;
        }
    }

    static class CapStringReaderProvider extends ReaderWriterProvider {
        @Override
        public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
            if (type.equals(String.class)) {
                return new CapStringReader();
            }
            return null;
        }
    }

    static class OverrideStringReaderProvider extends ReaderWriterProvider {
        final ValueReader vr;

        public OverrideStringReaderProvider(String str) {
            vr = new OverrideStringReader(str);
        }

        @Override
        public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
            if (type.equals(String.class)) {
                return vr;
            }
            return null;
        }
    }
    
    static class Point {
        public int _x, _y;

        public Point(int x, int y) {
            _x = x;
            _y = y;
        }
    }

    static class PointReader extends ValueReader {
        public PointReader() { super(Point.class); }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            Map<String, Object> map = reader.readMap();
            return new Point((Integer) map.get("x"), (Integer) map.get("y"));
        }
    }

    static class PointReaderProvider extends ReaderWriterProvider {
        @Override
        public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
            if (type == Point.class) {
                return new PointReader();
            }
            return null;
        }
    }

    static class NoOpProvider extends ReaderWriterProvider {
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    @Test
    public void testCustomBeanReader() throws Exception
    {
        // First: without handler, will fail to map
        try {
            JSON.std.beanFrom(CustomValue.class, "123");
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, ".CustomValue");
            verifyException(e, "constructor to use");
        }

        // then with custom, should be fine
        JSON json = jsonWithProvider(new CustomReaders(0));
        CustomValue v = json.beanFrom(CustomValue.class, "123");
        assertEquals(124, v.value);

        // similarly with wrapper
        CustomValueBean bean = json.beanFrom(CustomValueBean.class,
                a2q("{ 'custom' : 137 }"));
        assertEquals(138, bean.custom.value);

        // but also ensure we can change registered handler(s)
        JSON json2 = jsonWithProvider(new CustomReaders(100));
        v = json2.beanFrom(CustomValue.class, "123");
        assertEquals(224, v.value);
    }

    @Test
    public void testChainedCustomBeanReaders() throws Exception
    {
        JSON json = jsonWithProviders(new CustomReaders(0),
                new CustomReaders(100));
        CustomValue v = json.beanFrom(CustomValue.class, "69");
        assertEquals(70, v.value);

        json = jsonWithProviders(new CustomReaders(100),
                new CustomReaders(0));
        v = json.beanFrom(CustomValue.class, "72");
        assertEquals(173, v.value);
    }

    @Test
    public void testCustomEnumReader() throws Exception
    {
        // First: without handler, will fail to map
        try {
            JSON.std.beanFrom(ABC.class, q("n/a"));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Failed to find Enum of type");
        }

        // then with custom, should be fine
        JSON json = jsonWithProvider(new CustomReaders(0));
        ABC v = json.beanFrom(ABC.class, q("n/a"));
        assertEquals(ABC.DEF, v);

        // but if we remove, again error
        JSON json2 = jsonWithProvider((ReaderWriterProvider) null);
        try {
            json2.beanFrom(ABC.class, q("n/a"));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Failed to find Enum of type");
        }
    }

    // Even more fun, override default String deserializer!
    @Test
    public void testCustomStringReader() throws Exception
    {
        String allCaps = jsonWithProvider(new CapStringReaderProvider())
                .beanFrom(String.class, q("Some text"));
        assertEquals("SOME TEXT", allCaps);
    }

    @Test
    public void testChainedStringReaders() throws Exception {
        String result = jsonWithProviders(new CapStringReaderProvider(),
                new OverrideStringReaderProvider("foo"))
                .beanFrom(String.class, q("Some text"));
        assertEquals("SOME TEXT", result);

        result = jsonWithProviders(new NoOpProvider(), new OverrideStringReaderProvider("foo"))
            .beanFrom(String.class, q("Some text"));
        assertEquals("foo", result);

        // and ok not to have anything, too
        result = jsonWithProviders(new NoOpProvider(), new NoOpProvider())
                .beanFrom(String.class, q("Some text"));
        assertEquals("Some text", result);

        // Plus nulls fine too
        result = jsonWithProviders(null, new OverrideStringReaderProvider("foo"))
                .beanFrom(String.class, q("Some text"));
        assertEquals("foo", result);
        result = jsonWithProviders(new OverrideStringReaderProvider("foo"), null)
                .beanFrom(String.class, q("Some text"));
        assertEquals("foo", result);
    }

    // But also can use methods from "JSONReader" for convenience
    @Test
    public void testCustomDelegatingReader() throws Exception
    {
        // First: without handler, will fail to map
        final String doc = "{\"y\" : 3, \"x\": 2 }";
        try {
            JSON.std.beanFrom(Point.class, doc);
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "$Point");
            verifyException(e, "constructor to use");
        }

        // then with custom, should be fine
        JSON json = jsonWithProvider(new PointReaderProvider());
        Point v = json.beanFrom(Point.class, doc);
        assertEquals(2, v._x);
        assertEquals(3, v._y);
    }
}
