package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.jr.ob.JSON;

public class BasicIgnoralTest extends ASTestBase
{
    static class XY {
        @JsonIgnore
        public int x;
        public int y;

        protected XY() { }
        public XY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class BaseXY {
        public int x, y;

        protected BaseXY() { }
        protected BaseXY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonIgnoreProperties({ "y" })
    static class XYZ
        extends BaseXY
    {
        public int z;

        protected XYZ() { }
        public XYZ(int x, int y, int z) {
            super(x, y);
            this.z = z;
        }
    }

    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport();

    /*
    /**********************************************************************
    /* Tests for basic @JsonIgnore
    /**********************************************************************
     */
    
    public void testPropertyIgnoralOnSerialize() throws Exception
    {
        final XY input = new XY(1, 2);
        // default, no ignorals:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));

        // but if we ignore 'x'...
        assertEquals(a2q("{'y':2}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));
    }

    public void testPropertyIgnoralOnDeserialize() throws Exception
    {
        final String json = a2q("{'x':1,'y':2}");

        // default: no filtering by ignorals
        XY result = JSON.std.beanFrom(XY.class, json);
        assertEquals(1, result.x);
        assertEquals(2, result.y);

        // but with ignore, should skip
        result = JSON_WITH_ANNO.beanFrom(XY.class, json);
        assertEquals(0, result.x);
        assertEquals(2, result.y);

        // and once again verify non-stickiness
        result = JSON.std.beanFrom(XY.class, json);
        assertEquals(1, result.x);
        assertEquals(2, result.y);
    }

    public void testPropertyIgnoreWithUnknown() throws Exception
    {
        final JSON jsonNoUnknowns = JSON_WITH_ANNO.with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);
        XY result = jsonNoUnknowns.beanFrom(XY.class, a2q("{'x':1,'y':2}"));
        // should read 'y', but not 'x'
        assertEquals(2, result.y);
        assertEquals(new XY().x, result.x);
    }

    /*
    /**********************************************************************
    /* Tests for @JsonIgnoreProperties
    /**********************************************************************
     */

    public void testClassIgnoralOnSerialize() throws Exception
    {
        final XYZ input = new XYZ(1, 2, 3);
        assertEquals(a2q("{'x':1,'z':3}"), JSON_WITH_ANNO.asString(input));
    }

    public void testClassIgnoralOnDeserialize() throws Exception
    {
        // First, regular ignoral (with skipped unknowns)
        final XYZ result = JSON_WITH_ANNO.beanFrom(XYZ.class, a2q("{'x':1,'y':2,'z':3}"));
        assertEquals(1, result.x);
        assertEquals(0, result.y);
        assertEquals(3, result.z);
    }

    public void testClassIgnoralOnDeserializeWithUnknown() throws Exception
    {
        final JSON jsonNoUnknowns = JSON_WITH_ANNO.with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);
        final XYZ result = jsonNoUnknowns.beanFrom(XYZ.class, a2q("{'x':1,'y':2,'z':3}"));
        assertEquals(1, result.x);
        assertEquals(0, result.y);
        assertEquals(3, result.z);
    }
}
