package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class BasicRenameTest extends ASTestBase
{
    static class NameSimple {
        @JsonProperty("firstName")
        public String _first;

        @JsonProperty // just explicit marker, no rename
        public String _last;

        protected NameSimple() { }
        public NameSimple(String f, String l) {
            _first = f;
            _last = l;
        }
    }

    // for stricter validation, fail on unknown properties
    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport()
            .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

    public void testBasicRenameOnSerialize() throws Exception
    {
        final NameSimple input = new NameSimple("Bob", "Burger");
        // default, no ignorals:
        assertEquals(a2q("{'_first':'Bob','_last':'Burger'}"), JSON.std.asString(input));

        // but if we ignore 'x'...
        assertEquals(a2q("{'_last':'Burger','firstName':'Bob'}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(a2q("{'_first':'Bob','_last':'Burger'}"), JSON.std.asString(input));
    }

    public void testBasicRenameOnDeserialize() throws Exception
    {
        final String json = a2q("{'firstName':'Bob','_last':'Burger'}");
        final JSON j = JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

        try {
            j.beanFrom(NameSimple.class, json);
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Unrecognized JSON property 'firstName'");
        }

        NameSimple result = JSON_WITH_ANNO.beanFrom(NameSimple.class, json);
        assertEquals("Bob", result._first);
        assertEquals("Burger", result._last);
    }
}
