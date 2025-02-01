package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// for [jackson-jr#25], allowing single-int constructors
public class ReadWithCtors25Test extends TestBase
{
    static class FromInt1 {
        protected int value;
        public FromInt1(int v) { value = v; }
    }

    static class FromInt2 {
        protected int value;
        public FromInt2(Integer v) { value = v.intValue(); }
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    @Test
    public void testIntCtor() throws Exception
    {
        FromInt1 output = JSON.std.beanFrom(FromInt1.class, "123");
        assertNotNull(output);
        assertEquals(123L, output.value);

        FromInt2 output2 = JSON.std.beanFrom(FromInt2.class, "456");
        assertNotNull(output2);
        assertEquals(456L, output2.value);
    }
}
