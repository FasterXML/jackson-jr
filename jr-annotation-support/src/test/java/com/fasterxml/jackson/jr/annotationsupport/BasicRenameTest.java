package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.jr.ob.JSON;

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

    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport();
    
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

}
