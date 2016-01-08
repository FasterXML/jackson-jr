package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.core.TreeNode;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

public abstract class TestBase extends TestCase
{
    protected final JsonFactory _factory = new JsonFactory();

    protected String writeTree(TreeCodec treeCodec, TreeNode treeNode) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator g = _factory.createGenerator(writer);
        treeCodec.writeTree(g, treeNode);
        g.close();
        return writer.toString();
    }

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
}
