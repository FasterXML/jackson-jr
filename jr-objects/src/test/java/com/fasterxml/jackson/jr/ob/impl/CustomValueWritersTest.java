package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CustomValueWritersTest extends TestBase
{
    static class CustomBean { }

    static class CustomBeanAsStringWriter implements ValueWriter {
        public final String text;

        public CustomBeanAsStringWriter(String v) { text = v; }
        
        @Override
        public void writeValue(JSONWriter context, JsonGenerator g,
                Object v) throws IOException {
            g.writeString(text);
        }

        @Override
        public Class<?> valueType() { return CustomBean.class; }
    }

    static class CustomBeanAsAnswerWriter implements ValueWriter {
        @Override
        public void writeValue(JSONWriter context, JsonGenerator g,
                Object v) throws IOException {
            g.writeStartObject();
            g.writeNumberField("answer", 42);
            g.writeNullField("none");
            g.writeEndObject();
        }

        @Override
        public Class<?> valueType() { return CustomBean.class; }
    }
    
    static class CustomBeanWrapper {
        public CustomBean wrapped = new CustomBean();
    }

    static class CustomWriters extends ReaderWriterProvider {
        private final String _str;

        public CustomWriters() { this("xxx"); }
        public CustomWriters(String str) { _str = str; }

        @Override
        public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
            if (type == CustomBean.class) {
                return new CustomBeanAsStringWriter(_str);
            }
            return null;
        }
    }

    static class CustomWriters42 extends ReaderWriterProvider {
        @Override
        public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
            if (type == CustomBean.class) {
                return new CustomBeanAsAnswerWriter();
            }
            return null;
        }
    }
    
    static class BogusProvider extends ReaderWriterProvider {
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    @Test
    public void testCustomBeanWriter() throws Exception
    {
        // without handler, empty "bean":
        assertEquals("{}", JSON.std.asString(new CustomBean()));
        assertEquals("{\"wrapped\":{}}", JSON.std.asString(new CustomBeanWrapper()));

        final JSON withCustom = jsonWithProvider(new CustomWriters());
        assertEquals(q("xxx"), withCustom.asString(new CustomBean()));
        assertEquals("{\"wrapped\":\"xxx\"}", withCustom.asString(new CustomBeanWrapper()));
        assertEquals("[\"xxx\"]", withCustom.asString(new CustomBean[] { new CustomBean() }));
        assertEquals("{\"value\":\"xxx\"}",
                withCustom.asString(Collections.singletonMap("value", new CustomBean())));

        // plus also should be able to create new instance with different representation
        final JSON withCustom42 = jsonWithProvider(new CustomWriters42());
        assertEquals("{\"answer\":42,\"none\":null}", withCustom42.asString(new CustomBean()));
        assertEquals("{\"wrapped\":{\"answer\":42,\"none\":null}}",
                withCustom42.asString(new CustomBeanWrapper()));
    }

    @Test
    public void testChainedBeanWriters() throws Exception
    {
        assertEquals(q("abc"),
                jsonWithProviders(new CustomWriters("abc"), new CustomWriters("def"))
                .asString(new CustomBean()));
        assertEquals(q("def"),
                jsonWithProviders(new BogusProvider(), new CustomWriters("def"))
                .asString(new CustomBean()));

        // as well as passing `null`
        assertEquals(q("xxx"),
                jsonWithProviders(null, new CustomWriters("xxx"))
                .asString(new CustomBean()));
        assertEquals(q("yyy"),
                jsonWithProviders(new CustomWriters("yyy"), null)
                .asString(new CustomBean()));
    }
}
