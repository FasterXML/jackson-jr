package com.fasterxml.jackson.jr.failing;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;

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

    public void testPojoWithIsGetter() throws Exception
    {
        String json;

        json = JSON.std.asString(new IsBean());
        // by default, will use 'is-getter':
        assertEquals(aposToQuotes("{'enabled':true,'value':42}"), json);

        // but can disable
        json = JSON.std
                .without(JSON.Feature.USE_IS_GETTERS)
                .asString(new IsBean());
        assertEquals(aposToQuotes("{'value':42}"), json);
    }

}
