package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;

public class ValueWriterModifierTest extends TestBase
{
    static class RWModifier extends ReaderWriterModifier {
        private final Class<?> _target;
        private final ValueWriter _writer;

        public RWModifier(Class<?> target, ValueWriter w) {
            _target = target;
            _writer = w;
        }

        @Override
        public ValueWriter modifyValueWriter(JSONWriter writeContext,
                Class<?> type, ValueWriter defaultWriter) {
            if (type == _target) {
                return _writer;
            }
            return defaultWriter;
        }

        @Override
        public ValueWriter overrideStandardValueWriter(JSONWriter writeContext,
                Class<?> type, int stdTypeId) {
            if (type == _target) {
                return _writer;
            }
            return null;
        }
    }

    static class ArrayingWriter implements ValueWriter
    {
        private final ValueWriter _origWriter;

        public ArrayingWriter(ValueWriter orig) {
            _origWriter = orig;
        }

        @Override
        public void writeValue(JSONWriter context, JsonGenerator g,
                Object value) throws IOException
        {
            g.writeStartArray();
            _origWriter.writeValue(context, g, value);
            g.writeEndArray();
        }

        @Override
        public Class<?> valueType() {
            return Object.class;
        }
    }

    static class ArrayingWriterModifier extends ReaderWriterModifier
    {
        @Override
        public ValueWriter modifyValueWriter(JSONWriter writeContext,
                Class<?> type, ValueWriter defaultWriter) {
            if (type == NameBean.class) {
                return new ArrayingWriter(defaultWriter);
            }
            return defaultWriter;
        }
    }

    static class Name {
        public String first, last;

        public Name(String f, String l) {
            first = f;
            last = l;
        }
    }
    
    /*
    /**********************************************************************
    /* Tests for wholesale replacement of `ValueReader`
    /**********************************************************************
     */
    
    public void testStringWriterReplacement() throws Exception
    {
        final ReaderWriterModifier mod = new RWModifier(String.class,
                new ValueWriter() {
                    @Override
                    public void writeValue(JSONWriter context, JsonGenerator g,
                            Object value) throws IOException {
                        g.writeString(String.valueOf(value).toUpperCase());
                    }

                    @Override
                    public Class<?> valueType() {
                        return String.class;
                    }
        });
        final String input = "foobar";
        final JSON jsonWithMod = jsonWithModifier(mod);
        String result = jsonWithMod.asString(input);
        assertEquals(q("FOOBAR"), result);
        // but also verify that no caching occurs wrt global standard variant:
        assertEquals(q("foobar"), JSON.std.asString(input));

        // And then also applicable for multiple POJO properties
        assertEquals(a2q("{'first':'BOB','last':'HOPE'}"),
                jsonWithMod.asString(new Name("Bob", "Hope")));

        // .. and not global standard variant
        assertEquals(a2q("{'first':'Bob','last':'Hope'}"),
                JSON.std.asString(new Name("Bob", "Hope")));
    }

    public void testPOJOWriterReplacement() throws Exception
    {
        final ReaderWriterModifier mod = new RWModifier(NameBean.class,
                new ValueWriter() {
            @Override
            public void writeValue(JSONWriter context, JsonGenerator g,
                    Object value) throws IOException {
                NameBean nb = (NameBean) value;
                g.writeString(nb.getFirst() + "-" + nb.getLast());
            }

            @Override
            public Class<?> valueType() {
                return NameBean.class;
            }
        });
        final NameBean input = new NameBean("Foo", "Bar");
        String json = jsonWithModifier(mod).asString(input);
        assertEquals(q("Foo-Bar"), json);
        // but also verify that no caching occurs wrt global standard variant:
        assertEquals(a2q("{'first':'Foo','last':'Bar'}"),
                JSON.std.asString(input));
    }

    public void testPOJOWriterDelegatingReplacement() throws Exception
    {
        final NameBean input = new NameBean("Foo", "Bar");
        String json = jsonWithModifier(new ArrayingWriterModifier())
            .asString(input);
        assertEquals(a2q("[{'first':'Foo','last':'Bar'}]"), json);

        // but also verify that no caching occurs wrt global standard variant:
        assertEquals(a2q("{'first':'Foo','last':'Bar'}"),
                JSON.std.asString(input));
    }
}
