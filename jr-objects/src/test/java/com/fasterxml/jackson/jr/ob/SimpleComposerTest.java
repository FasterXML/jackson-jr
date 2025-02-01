package com.fasterxml.jackson.jr.ob;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleComposerTest extends TestBase
{
    public static class NameBean {
        String first;

        public NameBean(String f) {
            first = f;
        }
        
        public String getFirst() { return first; }
        public void setFirst(String s) { first = s; }
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testListComposer() throws Exception
    {
        List<Object> list = JSON.std
                .composeCollection(new ArrayList<Object>())
                .add(true)
                .add("foo")
                .add(13)
                .startArray()
                    .add(55)
                .end()
                .finish();
        assertEquals("[true,\"foo\",13,[55]]", JSON.std.asString(list));

        list = JSON.std.composeList()
                .add(-3)
                .startObject()
                    .put("a", 1)
                    .put("b", 2)
                .end()
                .finish();
        assertEquals("[-3,{\"a\":1,\"b\":2}]", JSON.std.asString(list));
    }

    @Test
    public void testComposerWithPojo() throws Exception
    {
        String json = JSON.std.composeString()
                .startArray()
                    .addObject(new NameBean("Bob"))
                    .startObject()
                        .putObject("name", new NameBean("Bill"))
                    .end()
                .end()
                .finish();
        assertEquals(a2q("[{'first':'Bob'},{'name':{'first':'Bill'}}]"), json);
    }

    @Test
    public void testComposerWithIndent() throws Exception
    {
        String json = JSON.std
                .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                .composeString()
                .startObject()
                    .put("name", "Bill")
                .end()
                .finish();
        assertEquals(a2q("{\n"
                +"  'name' : 'Bill'\n"
                +"}"),
                json);
    }

    @Test
    public void testSimpleComposeMap() throws Exception
    {
        Map<String, Object> map = JSON.std.composeMap()
                .put("answer", 42)
                .finish();
        assertEquals(1, map.size());
        assertEquals("{answer=42}", map.toString());
    }
}
