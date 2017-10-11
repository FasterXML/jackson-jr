package com.fasterxml.jackson.jr.stree;

import java.io.StringWriter;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Tests for reading content using {@link JSON} with proper
 * codec registration
 */
public class ReadViaJSONTest extends TestBase
{
     private final TreeCodec TREE_CODEC = new JacksonJrsTreeCodec();
     private final JSON treeJSON = JSON.std.with(TREE_CODEC);

     public void testSimpleList() throws Exception
     {
          final String INPUT = "[true,\"abc\"]";
         // and then through jr-objects:
          TreeNode node = treeJSON.treeFrom(INPUT);

         assertTrue(node instanceof JrsArray);
         assertEquals(2, node.size());
         // actually, verify with write...
         final StringWriter writer = new StringWriter();
         final JsonGenerator g = _factory.createGenerator(emptyWriteContext(),
                 writer);
         TREE_CODEC.writeTree(g, node);
         g.close();
         assertEquals(INPUT, writer.toString());
     }

     public void testSimpleMap() throws Exception
     {
         final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
         TreeNode node = treeJSON.treeFrom(INPUT);
         assertTrue(node instanceof JrsObject);
         assertEquals(3, node.size());
         // actually, verify with write...
         final StringWriter writer = new StringWriter();
         final JsonGenerator g = _factory.createGenerator(writer);
         TREE_CODEC.writeTree(g, node);
         g.close();
         assertEquals(INPUT, writer.toString());
     }

     public void testSimpleMixed() throws Exception
     {
         final String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
         TreeNode node = treeJSON.treeFrom(INPUT);
         assertTrue(node instanceof JrsObject);
         assertEquals(2, node.size());
         TreeNode list = node.get("a");
         assertTrue(list instanceof JrsArray);

         // actually, verify with write...
         final StringWriter writer = new StringWriter();
         final JsonGenerator g = _factory.createGenerator(writer);
         TREE_CODEC.writeTree(g, node);
         g.close();
         assertEquals(INPUT, writer.toString());
     }
}
