package com.fasterxml.jackson.simple.ob;

public class SimpleComposerTest extends TestBase
{
    public void testSimpleNonNestedObject() throws Exception
    {
        String json = JSON.std.composeString()
                .startObject()
                    .put("a", 1)
                    .put("b", false)
                .end()
                .finish();
        assertEquals("{\"a\":1,\"b\":false}", json);
    }

    public void testSimpleNonNestedArray() throws Exception
    {
        String json = JSON.std.composeString()
                .startArray()
                    .add(true)
                    .add("abc")
                    .add(75)
                .end()
                .finish();
        assertEquals("[true,\"abc\",75]", json);
    }

    public void testNestedObject() throws Exception
    {
        String json = JSON.std.composeString()
                .startObject()
                    .put("a", 1)
                    .startObjectField("ob")
                        .startObjectField("x")
                            .startObjectField("inner")
                            .end()
                        .end()
                    .end()
                .end()
                .finish();
        assertEquals("{\"a\":1,\"ob\":{\"x\":{\"inner\":{}}}}", json);
    }

    public void testNestedArray() throws Exception
    {
        String json = JSON.std.composeString()
                .startArray()
                    .startArray()
                        .add(true)
                        .startArray()
                            .startArray().end()
                            .add(13)
                        .end()
                    .end()
                .end()
                .finish();
        assertEquals("[[true,[[],13]]]", json);
    }
    
    public void testNestedMixed() throws Exception
    {
        byte[] json = JSON.std.composeBytes()
                .startObject()
                    .put("a", 1)
                    .startArrayField("arr")
                        .add(1).add(2).add(3)
                    .end()
                    .startObjectField("ob")
                        .put("x", 3)
                        .put("y", 4)
                        .startArrayField("args").add("none").end()
                    .end()
                    .put("last", true)
                .end()
                .finish();
        assertEquals("{\"a\":1,\"arr\":[1,2,3],\"ob\":{\"x\":3,\"y\":4,"
                +"\"args\":[\"none\"]},\"last\":true}",
            new String(json, "UTF-8"));
    }
}
