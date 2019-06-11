package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.*;

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
            g.writeEndObject();
        }

        @Override
        public Class<?> valueType() { return CustomBean.class; }
    }
    
    static class CustomBeanWrapper {
        public CustomBean wrapped = new CustomBean();
    }
    
    static class CustomWriters extends ReaderWriterProvider {
        @Override
        public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
            if (type == CustomBean.class) {
                return new CustomBeanAsStringWriter("xxx");
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
    
    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    public void testCustomBeanReader() throws Exception
    {
        // without handler, empty "bean":
        assertEquals("{}", JSON.std.asString(new CustomBean()));
        assertEquals("{\"wrapped\":{}}", JSON.std.asString(new CustomBeanWrapper()));

        final JSON withCustom = JSON.std
                .with(new CustomWriters());
        assertEquals(quote("xxx"), withCustom.asString(new CustomBean()));
        assertEquals("{\"wrapped\":\"xxx\"}", withCustom.asString(new CustomBeanWrapper()));
        assertEquals("[\"xxx\"]", withCustom.asString(new CustomBean[] { new CustomBean() }));
        assertEquals("{\"value\":\"xxx\"}",
                withCustom.asString(Collections.singletonMap("value", new CustomBean())));

        // plus also should be able to create new instance with different representation
        final JSON withCustom42 = withCustom
                .with(new CustomWriters42());
        assertEquals("{\"answer\":42}", withCustom42.asString(new CustomBean()));
        assertEquals("{\"wrapped\":{\"answer\":42}}",
                withCustom42.asString(new CustomBeanWrapper()));
    }

}
