package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Trivial tests for wiring; not particularly useful as STree instances are immutable
 * so while Array/Object nodes may be created, they will be empty and can not
 * really be changed.
 */
public class CreateNodesTest extends JacksonJrTreeTestBase
{
     private final JSON treeJSON = jsonWithTreeCodec();

     public void testCreateArrayNode() throws Exception
     {
          JrsArray node = treeJSON.createArrayNode();
          assertNotNull(node);
          assertEquals(0, node.size());
     }
     
     public void testCreateObjectNode() throws Exception
     {
         JrsObject node = treeJSON.createObjectNode();
         assertNotNull(node);
         assertEquals(0, node.size());
     }
}
