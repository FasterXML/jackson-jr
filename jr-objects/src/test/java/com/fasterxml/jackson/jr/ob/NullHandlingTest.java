package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NullHandlingTest extends TestBase
{
    public static class Bean107 {
        public byte[] b = new byte[0];
    }

    public static class StringBean {
        public String str = "a";
    }

    // [jackson-jr#78]
    public static class IntegerWrapper {
        public Integer value;
    }
    public static class IntPrimitiveWrapper {
        public int value;
    }
    public static class LongWrapper {
        public Long value;
    }
    public static class LongPrimitiveWrapper {
        public long value;
    }
    public static class FloatWrapper {
        public Float value;
    }
    public static class FloatPrimitiveWrapper {
        public float value;
    }
    public static class DoubleWrapper {
        public Double value;
    }
    public static class DoublePrimitiveWrapper {
        public double value;
    }
    public static class BooleanWrapper {
        public Boolean value;
    }
    public static class BooleanPrimitiveWrapper {
        public boolean value;
    }

    // Test to verify that outputting of nulls is configurable
    @Test
    public void testMapNullEntries() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("a", 1);
        map.put("b", null);
        // By default we do NOT write null-valued entries:
        assertEquals("{\"a\":1}", JSON.std.asString(map));
        // but we can disable it easily
        assertEquals("{\"a\":1,\"b\":null}",
                JSON.std.with(JSON.Feature.WRITE_NULL_PROPERTIES).asString(map));
    }

    @Test
    public void testNullForString() throws Exception
    {
        assertNull(JSON.std.beanFrom(StringBean.class, "null"));

        StringBean bean = JSON.std.beanFrom(StringBean.class, a2q("{'str':null}"));
        assertNull(bean.str);
    }

    // [jackson-jr#107]: nulls should be accepted as byte[]
    @Test
    public void testNullForByteArray() throws Exception
    {
        assertNull(JSON.std.beanFrom(Bean107.class, "null"));

        Bean107 bean = JSON.std.beanFrom(Bean107.class, a2q("{'b':null}"));
        assertNull(bean.b);
    }

    // [jackson-jr#78], int/Integer

    @Test
    public void testIntPrimitive() throws Exception
    {
        IntPrimitiveWrapper w = JSON.std.beanFrom(IntPrimitiveWrapper.class,
                a2q("{'value':1}"));
        assertEquals(1, w.value);

        w = JSON.std.beanFrom(IntPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0, w.value);
    }

    @Test
    public void testIntWrapper() throws Exception
    {
        IntegerWrapper w = JSON.std.beanFrom(IntegerWrapper.class,
                a2q("{'value':1}"));
        assertEquals(Integer.valueOf(1), w.value);

        w = JSON.std.beanFrom(IntegerWrapper.class,
                a2q("{'value':null}"));
        assertNull(w.value);
    }

    // [jackson-jr#78], long/Long

    @Test
    public void testLongPrimitive() throws Exception
    {
        LongPrimitiveWrapper w = JSON.std.beanFrom(LongPrimitiveWrapper.class,
                a2q("{'value':2}"));
        assertEquals(2L, w.value);

        w = JSON.std.beanFrom(LongPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0L, w.value);
    }

    @Test
    public void testLongWrapper() throws Exception
    {
        LongWrapper w = JSON.std.beanFrom(LongWrapper.class,
                a2q("{'value':2}"));
        assertEquals(Long.valueOf(2L), w.value);

        w = JSON.std.beanFrom(LongWrapper.class,
                a2q("{'value':null}"));
        assertNull(w.value);
    }

    // [jackson-jr#78], float/Float

    @Test
    public void testFloatPrimitive() throws Exception
    {
        FloatPrimitiveWrapper w = JSON.std.beanFrom(FloatPrimitiveWrapper.class,
                a2q("{'value':0.25}"));
        assertEquals(0.25f, w.value);

        w = JSON.std.beanFrom(FloatPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0.0f, w.value);
    }

    @Test
    public void testFloatWrapper() throws Exception
    {
        FloatWrapper w = JSON.std.beanFrom(FloatWrapper.class,
                a2q("{'value':0.25}"));
        assertEquals(Float.valueOf(0.25f), w.value);

        w = JSON.std.beanFrom(FloatWrapper.class,
                a2q("{'value':null}"));
        assertNull(w.value);
    }

    // [jackson-jr#78], double/Double

    @Test
    public void testDoublePrimitive() throws Exception
    {
        DoublePrimitiveWrapper w = JSON.std.beanFrom(DoublePrimitiveWrapper.class,
                a2q("{'value':0.25}"));
        assertEquals(0.25, w.value);

        w = JSON.std.beanFrom(DoublePrimitiveWrapper.class,
                a2q("{'value':null}"));
        // yeah yeah, not kosher wrt epsilon etc but...
        assertEquals(0.0, w.value);
    }

    @Test
    public void testDoubleWrapper() throws Exception
    {
        DoubleWrapper w = JSON.std.beanFrom(DoubleWrapper.class,
                a2q("{'value':0.25}"));
        assertEquals(Double.valueOf(0.25), w.value);

        w = JSON.std.beanFrom(DoubleWrapper.class,
                a2q("{'value':null}"));
        assertNull(w.value);
    }

    // [jackson-jr#78], boolean/Boolean

    @Test
    public void testBooleanPrimitive() throws Exception
    {
        BooleanPrimitiveWrapper w = JSON.std.beanFrom(BooleanPrimitiveWrapper.class,
                a2q("{'value':true}"));
        assertTrue(w.value);

        w = JSON.std.beanFrom(BooleanPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertFalse(w.value);
    }

    @Test
    public void testBooleanWrapper() throws Exception
    {
        BooleanWrapper w = JSON.std.beanFrom(BooleanWrapper.class,
                a2q("{'value':true}"));
        assertEquals(Boolean.TRUE, w.value);

        w = JSON.std.beanFrom(BooleanWrapper.class,
                a2q("{'value':null}"));
        assertNull(w.value);
    }
}
