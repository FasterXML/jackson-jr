package com.fasterxml.jackson.jr.ob.impl;

import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

public class ValueReaderModifierTest extends TestBase
{
    static class RWModifier extends ReaderWriterModifier {
        private final Class<?> _target;
        private final ValueReader _reader;

        public RWModifier(Class<?> target, ValueReader r) {
            _target = target;
            _reader = r;
        }
        
        @Override
        public ValueReader modifyValueReader(JSONReader readContext,
                Class<?> type, ValueReader defaultReader) {
            if (type == _target) {
                return _reader;
            }
            return defaultReader;
        }
    }

    static class NameLowerCasingReader extends ValueReader
    {
        private final ValueReader _origReader;

        public NameLowerCasingReader(ValueReader orig) {
            super(NameBean.class);
            _origReader = orig;
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) {
            NameBean nb = (NameBean) _origReader.read(reader, p);
            return new NameBean(nb.getFirst().toLowerCase(),
                    nb.getLast().toLowerCase());
        }
    }

    static class LowerCasingReaderModifier extends ReaderWriterModifier {
        public LowerCasingReaderModifier() { }
        
        @Override
        public ValueReader modifyValueReader(JSONReader readContext,
                Class<?> type, ValueReader defaultReader) {
            if (type == NameBean.class) {
                return new NameLowerCasingReader(defaultReader);
            }
            return defaultReader;
        }
    }
    
    /*
    /**********************************************************************
    /* Tests for wholesale replacement of `ValueReader`
    /**********************************************************************
     */
    
    public void testStringReaderReplacement() throws Exception
    {
        final String input = quote("foobar");
        assertEquals("foobar", JSON.std.beanFrom(String.class, input));

        // but then with modifier
        final ReaderWriterModifier mod = new RWModifier(String.class,
                new ValueReader(String.class) {
            @Override
            public Object read(JSONReader reader, JsonParser p) {
                return p.getText().toUpperCase();
            };
        });
        assertEquals("FOOBAR",
                jsonWithModifier(mod).beanFrom(String.class, input));

        // but also verify that no caching occurs wrt global standard variant:
        assertEquals("foobar", JSON.std.beanFrom(String.class, input));
    }

    public void testPOJOReaderReplacement() throws Exception
    {
        final ReaderWriterModifier mod = new RWModifier(NameBean.class,
                new ValueReader(NameBean.class) {
            @Override
            public Object read(JSONReader reader, JsonParser p) {
                Map<?, Object> map = reader.readMap();
                return new NameBean(String.valueOf(map.get("first")).toUpperCase(),
                        String.valueOf(map.get("last")).toUpperCase());
            };
        });
        final String input = aposToQuotes("{'first':'foo', 'last':'bar'}");
        NameBean result = jsonWithModifier(mod).beanFrom(NameBean.class, input);
        assertEquals("FOO", result.getFirst());
        assertEquals("BAR", result.getLast());

        // also verify that no caching occurs wrt global standard variant
        result = JSON.std.beanFrom(NameBean.class, input);
        assertEquals("foo", result.getFirst());
        assertEquals("bar", result.getLast());
    }

    public void testPOJOReaderDelegation() throws Exception
    {
        final String input = aposToQuotes("{'first':'Foo', 'last':'Bar'}");
        NameBean result = jsonWithModifier(new LowerCasingReaderModifier())
                .beanFrom(NameBean.class, input);
        assertEquals("foo", result.getFirst());
        assertEquals("bar", result.getLast());

        // also verify that no caching occurs wrt global standard variant
        result = JSON.std.beanFrom(NameBean.class, input);
        assertEquals("Foo", result.getFirst());
        assertEquals("Bar", result.getLast());
    }
}
