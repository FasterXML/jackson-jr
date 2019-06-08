package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

public class CustomValueHandlersTest extends TestBase
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

    static class CustomValueReader extends ValueReader {
        public CustomValueReader() {
            super(CustomValue.class);
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            return new CustomValue(p.getIntValue(), true);
        }

        // Base class impl should be fine, although we'd use this for optimal
        /*
        @Override
        public Object readNext(JSONReader reader, JsonParser p) throws IOException {
            return new CustomValue(p.nextIntValue(-1), true);
        }
        */
    }

    static class CustomReaders extends ReaderWriterProvider {
        @Override
        public ValueReader findBeanReader(JSONReader readContext, Class<?> type) {
            if (type.equals(CustomValue.class)) {
                return new CustomValueReader();
            }
            return null;
        }
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    public void testSimpleCustomReader() throws Exception
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
        JSON json = JSON.std
                .with(new CustomReaders());
        CustomValue v = json.beanFrom(CustomValue.class, "123");
        assertEquals(124, v.value);

        // similarly with wrapper
        CustomValueBean bean = json.beanFrom(CustomValueBean.class,
                aposToQuotes("{ 'custom' : 137 }"));
        assertEquals(138, bean.custom.value);
    }
}
