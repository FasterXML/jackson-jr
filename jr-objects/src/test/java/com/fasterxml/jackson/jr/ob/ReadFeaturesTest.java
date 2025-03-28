package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadFeaturesTest extends TestBase
{
    static class IsBean {
        public boolean isEnabled() { return true; }

        public int getValue() { return 42; }
    }

    /*
    /**********************************************************************
    /* Test methdods
    /**********************************************************************
     */

    @Test
    public void testPojoWithIsGetter() throws Exception
    {
        assertTrue(JSON.std.isEnabled(JSON.Feature.USE_IS_GETTERS));
        
        String json;

        json = JSON.std.asString(new IsBean());
        // by default, will use 'is-getter':
        assertEquals(a2q("{'enabled':true,'value':42}"), json);

        // but can disable
        json = JSON.std
                .without(JSON.Feature.USE_IS_GETTERS)
                .asString(new IsBean());
        assertEquals(a2q("{'value':42}"), json);

        // .... as well as using alternative
        json = JSON.builder()
                .disable(JSON.Feature.USE_IS_GETTERS)
                .build()
                .asString(new IsBean());
        assertEquals(a2q("{'value':42}"), json);
        
        // and go back as well
        json = JSON.std
                .with(JSON.Feature.USE_IS_GETTERS)
                .asString(new IsBean());
        assertEquals(a2q("{'enabled':true,'value':42}"), json);
    }

    @Test
    public void testFailOnDupMapKeys() throws Exception
    {
        JSON j = JSON.builder()
                .enable(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
                .build();
        assertTrue(j.isEnabled(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS));
        final String json = "{\"a\":1,\"b\":2,\"b\":3,\"c\":4}";
        try {
            /*Map<?,?> map =*/ j.mapFrom(json);
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Duplicate key");
        }
    }
}
