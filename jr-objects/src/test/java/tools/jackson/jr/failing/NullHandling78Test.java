package tools.jackson.jr.failing;

import tools.jackson.jr.ob.JSON;
import tools.jackson.jr.ob.TestBase;

public class NullHandling78Test extends TestBase
{
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
    static class DoubleWrapper {
        public Double value;
    }
    static class DoublePrimitiveWrapper {
        public double value;
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

    // [jackson-jr#78], boolean/Boolean

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
}
