package com.fasterxml.jackson.jr.stree;

import java.util.*;

import tools.jackson.jr.ob.JSON;
import tools.jackson.jr.stree.JrsArray;
import tools.jackson.jr.stree.JrsBoolean;
import tools.jackson.jr.stree.JrsNumber;
import tools.jackson.jr.stree.JrsObject;
import tools.jackson.jr.stree.JrsString;
import tools.jackson.jr.stree.JrsValue;

public class WriteViaJSONTest extends JacksonJrTreeTestBase
{
     private final JSON treeJSON = jsonWithTreeCodec();

     public void testSimpleList() throws Exception
    {
        List<JrsValue> stuff = new LinkedList<JrsValue>();
        stuff.add(new JrsString("x"));
        stuff.add(JrsBoolean.TRUE);
        stuff.add(new JrsNumber(123));
        assertEquals("[\"x\",true,123]", treeJSON.asString(new JrsArray(stuff)));
    }

    public void testSimpleMap() throws Exception
    {
        Map<String,JrsValue> stuff = new LinkedHashMap<String,JrsValue>();
        stuff.put("a", new JrsNumber(15));
        stuff.put("b", JrsBoolean.TRUE);
        stuff.put("c", new JrsString("foobar"));

        assertEquals("{\"a\":15,\"b\":true,\"c\":\"foobar\"}",
                  treeJSON.asString(new JrsObject(stuff)));
    }

    public void testNest() throws Exception
    {
        Map<String,JrsValue> stuff = new LinkedHashMap<String,JrsValue>();
        List<JrsValue> list = new ArrayList<JrsValue>();
        list.add(new JrsNumber(123));
        list.add(new JrsNumber(456));
        stuff.put("first", new JrsArray(list));
        Map<String,JrsValue> second = new LinkedHashMap<String,JrsValue>();
        stuff.put("second", new JrsObject(second));
        second.put("foo", new JrsString("bar"));
        second.put("bar", new JrsArray());

        assertEquals("{\"first\":[123,456],\"second\":{\"foo\":\"bar\",\"bar\":[]}}",
                  treeJSON.asString(new JrsObject(stuff)));
    }
}
