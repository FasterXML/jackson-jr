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

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    // for stricter validation, fail on unknown properties
    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport()
            .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

    public void testBasicRenameOnSerialize() throws Exception
    {
        final NameSimple input = new NameSimple("Bob", "Burger");
        // default, no ignorals:
        assertEquals(a2q("{'_first':'Bob','_last':'Burger'}"), JSON.std.asString(input));

        // but if we rename "_last"
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
            verifyException(e, "Unrecognized JSON property \"firstName\"");
        }
        NameSimple result = JSON_WITH_ANNO.beanFrom(NameSimple.class, json);
        assertEquals("Bob", result._first);
        assertEquals("Burger", result._last);
    }

    public void testEnumRenameOnSerialize() throws Exception
    {
        ABCRename inputA = ABCRename.A;
        // default
        assertEquals(a2q("\"A\""), JSON.std.asString(inputA));
        // with annotations
        assertEquals(a2q("\"A1\""), JSON_WITH_ANNO.asString(inputA));

        ABCRename inputB = ABCRename.B;
        // default
        assertEquals(a2q("\"B\""), JSON.std.asString(inputB));
        // with annotations
        assertEquals(a2q("\"B1\""), JSON_WITH_ANNO.asString(inputB));

        ABCRename inputC = ABCRename.C;
        // default
        assertEquals(a2q("\"C\""), JSON.std.asString(inputC));
        // with annotations
        assertEquals(a2q("\"C\""), JSON_WITH_ANNO.asString(inputC));
    }

    public void testEnumRenameOnDeserialize() throws Exception
    {
        String jsonA = a2q("\"A1\"");
        ABCRename resultA = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonA);
        assertEquals(ABCRename.A, resultA);

        String jsonB = a2q("\"B1\"");
        ABCRename resultB = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonB);
        assertEquals(ABCRename.B, resultB);

        String jsonC = a2q("\"C\"");
        ABCRename resultC = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonC);
        assertEquals(ABCRename.C, resultC);
    }
}
