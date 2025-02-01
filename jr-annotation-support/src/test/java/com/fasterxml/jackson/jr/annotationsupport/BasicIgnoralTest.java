package com.fasterxml.jackson.jr.annotationsupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicIgnoralTest extends ASTestBase
{
    static class XY {
        public static int DEFAULT = 123;
        public static final int DEFAULT_FINAL = 123;
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
    private final JSON JSON_WITH_ANNO_WITH_STATIC =
            JSON.builder().register(JacksonAnnotationExtension.std).enable(JSON.Feature.INCLUDE_STATIC_FIELDS).build();
    private final JSON JSON_WITH_STATIC = JSON.builder().enable(JSON.Feature.INCLUDE_STATIC_FIELDS).build();

    /*
    /**********************************************************************
    /* Tests for basic @JsonIgnore
    /**********************************************************************
     */

    @Test
    public void testPropertyIgnoralOnSerialize() throws Exception
    {
        final XY input = new XY(1, 2);
        // default, no ignorals:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));

        // but if we ignore 'x'...
        assertEquals(a2q("{'y':2}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));

        // Verify serialization of static fields when the INCLUDE_STATIC_FIELDS option is enabled
        assertEquals(a2q("{'DEFAULT':123,'x':1,'y':2}"), JSON_WITH_STATIC.asString(input));
        assertEquals(a2q("{'DEFAULT':123,'y':2}"), JSON_WITH_ANNO_WITH_STATIC.asString(input));
    }

    @Test
    public void testPropertyIgnoralOnDeserialize() throws Exception
    {
        final String json = a2q("{'DEFAULT':125,'x':1,'y':2}");

        // default: no filtering by ignorals
        XY result = JSON.std.beanFrom(XY.class, json);
        assertEquals(1, result.x);
        assertEquals(2, result.y);
        assertEquals(XY.DEFAULT_FINAL, XY.DEFAULT);

        // but with ignore, should skip
        result = JSON_WITH_ANNO.beanFrom(XY.class, json);
        assertEquals(0, result.x);
        assertEquals(2, result.y);
        assertEquals(XY.DEFAULT_FINAL, XY.DEFAULT);

        // and once again verify non-stickiness
        result = JSON.std.beanFrom(XY.class, json);
        assertEquals(1, result.x);
        assertEquals(2, result.y);
        assertEquals(XY.DEFAULT_FINAL, XY.DEFAULT);

        // Verify setting static field from serialized data when the INCLUDE_STATIC_FIELDS option is enabled
        JSON_WITH_STATIC.beanFrom(XY.class, json);
        assertEquals(125, XY.DEFAULT);
        XY.DEFAULT = XY.DEFAULT_FINAL;
        JSON_WITH_ANNO_WITH_STATIC.beanFrom(XY.class, json);
        assertEquals(125, XY.DEFAULT);
        XY.DEFAULT = XY.DEFAULT_FINAL;
    }

    @Test
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

    @Test
    public void testClassIgnoralOnSerialize() throws Exception
    {
        final XYZ input = new XYZ(1, 2, 3);
        assertEquals(a2q("{'x':1,'z':3}"), JSON_WITH_ANNO.asString(input));
    }

    @Test
    public void testClassIgnoralOnDeserialize() throws Exception
    {
        // First, regular ignoral (with skipped unknowns)
        final XYZ result = JSON_WITH_ANNO.beanFrom(XYZ.class, a2q("{'x':1,'y':2,'z':3}"));
        assertEquals(1, result.x);
        assertEquals(0, result.y);
        assertEquals(3, result.z);
    }

    @Test
    public void testClassIgnoralOnDeserializeWithUnknown() throws Exception
    {
        final JSON jsonNoUnknowns = JSON_WITH_ANNO.with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);
        final XYZ result = jsonNoUnknowns.beanFrom(XYZ.class, a2q("{'x':1,'y':2,'z':3}"));
        assertEquals(1, result.x);
        assertEquals(0, result.y);
        assertEquals(3, result.z);
    }
}
