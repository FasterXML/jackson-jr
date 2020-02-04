package com.fasterxml.jackson.jr.stree;

import java.util.Arrays;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.ob.JSON;

import junit.framework.TestCase;

public abstract class TestBase extends TestCase
{
    protected final JsonFactory _factory = new JsonFactory();

    protected void assertToken(JsonToken expToken, JsonToken actToken)
    {
        if (actToken != expToken) {
            fail("Expected token "+expToken+", current token "+actToken);
        }
    }

    protected void assertToken(JsonToken expToken, JsonParser jp)
    {
        assertToken(expToken, jp.getCurrentToken());
    }

    protected void verifyException(Throwable e, String... matches)
    {
        String msg = e.getMessage();
        String lmsg = (msg == null) ? "" : msg.toLowerCase();
        for (String match : matches) {
            String lmatch = match.toLowerCase();
            if (lmsg.indexOf(lmatch) >= 0) {
                return;
            }
        }
        fail("Expected an exception with one of substrings ("+Arrays.asList(matches)+"): got one with message \""+msg+"\"");
    }

    protected String quote(String str) {
        return "\"" + str + "\"";
    }

    protected String aposToQuotes(String json) {
        return json.replace("'", "\"");
    }

    protected JSON jsonWithTreeCodec() {
        return JSON.builder()
            .treeCodec(new JacksonJrsTreeCodec())
            .build();
    }
}
