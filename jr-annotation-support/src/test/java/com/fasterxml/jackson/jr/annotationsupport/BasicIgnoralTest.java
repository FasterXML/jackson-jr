package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport();
    
    public void testIgnoralOnSerialize() throws Exception
    {
        final XY input = new XY(1, 2);
        // default, no ignorals:
        assertEquals(aposToQuotes("{'x':1,'y':2}"), JSON.std.asString(input));

        // but if we ignore 'x'...
        assertEquals(aposToQuotes("{'y':2}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(aposToQuotes("{'x':1,'y':2}"), JSON.std.asString(input));
    }
}
