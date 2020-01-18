package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

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

    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport();
    
    public void testIgnoralOnSerialize() throws Exception
    {
        final XY input = new XY(1, 2);
        // default, no ignorals:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));

        // but if we ignore 'x'...
        assertEquals(a2q("{'y':2}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));
    }

    public void testIgnoralOnDeserialize() throws Exception
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

    public void testIgnoraAndUnknown() throws Exception
    {
        final JSON jsonNoUnknowns = JSON_WITH_ANNO.with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);
        try {
            /* result = */ jsonNoUnknowns.beanFrom(XY.class, a2q("{'x':1,'y':2}"));
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Unrecognized JSON property 'x'");
        }
    }
}
