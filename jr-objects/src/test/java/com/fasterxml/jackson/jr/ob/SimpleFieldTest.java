package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleFieldTest extends TestBase
{
    public static class XY {
        public int x;
        private int y;

        public XY(int x0, int y0) {
            x = x0;
            y = y0;
        }
        protected XY() { }

        public int getY() { return y; }
        public void setY(int value) { y = value; }
    }

    @Test
    public void testDefaultSettings() {
        // Changed in 2.10
        assertTrue(JSON.std.isEnabled(JSON.Feature.USE_FIELDS));
    }

    @Test
    public void testSerializeWithoutField() throws Exception
    {
        String json = JSON.std.without(JSON.Feature.USE_FIELDS)
                .asString(new XY(1, 2));
        assertEquals(a2q("{'y':2}"), json);
    }

    @Test
    public void testSerializeWithField() throws Exception
    {
        String json = JSON.std.with(JSON.Feature.USE_FIELDS)
                .asString(new XY(1, 2));
        assertEquals(a2q("{'x':1,'y':2}"), json);
    }

    @Test
    public void testDeserializeWithField() throws Exception
    {
        XY result = JSON.std.with(JSON.Feature.USE_FIELDS)
                .beanFrom(XY.class, a2q("{'x':3,'y':4}"));
        assertEquals(4, result.getY());
        assertEquals(3, result.x);
    }
}
