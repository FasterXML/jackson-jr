package tools.jackson.jr.ob;

import java.util.LinkedHashMap;
import java.util.Map;

public class NullHandlingTest extends TestBase
{
    static class Bean107 {
        public byte[] b = new byte[0];
    }

    static class StringBean {
        public String str = "a";
    }

    // [jackson-jr#78]
    static class IntegerWrapper {
        public Integer value;
    }
    static class IntPrimitiveWrapper {
        public int value;
    }
    static class LongWrapper {
        public Long value;
    }
    static class LongPrimitiveWrapper {
        public long value;
    }
    static class FloatWrapper {
        public Float value;
    }
    static class FloatPrimitiveWrapper {
        public float value;
    }
    static class DoubleWrapper {
        public Double value;
    }
    static class DoublePrimitiveWrapper {
        public double value;
    }

    static class BooleanWrapper {
        public Boolean value;
    }
    static class BooleanPrimitiveWrapper {
        public boolean value;
    }

    // Test to verify that outputting of nulls is configurable
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

    public void testNullForString() throws Exception
    {
        assertNull(JSON.std.beanFrom(StringBean.class, "null"));

        StringBean bean = JSON.std.beanFrom(StringBean.class, a2q("{'str':null}"));
        assertNull(bean.str);
    }

    // [jackson-jr#107]: nulls should be accepted as byte[]
    public void testNullForByteArray() throws Exception
    {
        assertNull(JSON.std.beanFrom(Bean107.class, "null"));

        Bean107 bean = JSON.std.beanFrom(Bean107.class, a2q("{'b':null}"));
        assertNull(bean.b);
    }

    // [jackson-jr#78], int/Integer

    public void testIntPrimitive() throws Exception
    {
        IntPrimitiveWrapper w = JSON.std.beanFrom(IntPrimitiveWrapper.class,
                a2q("{'value':1}"));
        assertEquals(1, w.value);

        w = JSON.std.beanFrom(IntPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0, w.value);
    }

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

    public void testLongPrimitive() throws Exception
    {
        LongPrimitiveWrapper w = JSON.std.beanFrom(LongPrimitiveWrapper.class,
                a2q("{'value':2}"));
        assertEquals(2L, w.value);

        w = JSON.std.beanFrom(LongPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0L, w.value);
    }

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

    public void testFloatPrimitive() throws Exception
    {
        FloatPrimitiveWrapper w = JSON.std.beanFrom(FloatPrimitiveWrapper.class,
                a2q("{'value':0.25}"));
        assertEquals(0.25f, w.value);

        w = JSON.std.beanFrom(FloatPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertEquals(0.0f, w.value);
    }

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

    public void testBooleanPrimitive() throws Exception
    {
        BooleanPrimitiveWrapper w = JSON.std.beanFrom(BooleanPrimitiveWrapper.class,
                a2q("{'value':true}"));
        assertTrue(w.value);

        w = JSON.std.beanFrom(BooleanPrimitiveWrapper.class,
                a2q("{'value':null}"));
        assertFalse(w.value);
    }

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
