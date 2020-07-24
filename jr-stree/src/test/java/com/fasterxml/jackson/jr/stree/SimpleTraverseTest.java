package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;

public class SimpleTraverseTest extends JacksonJrTreeTestBase
{
    private final TreeCodec TREE_CODEC = new JacksonJrsTreeCodec();

  private final static ObjectReadContext EMPTY_READ_CONTEXT = new ObjectReadContext.Base();
    
    public void testSimpleObject() throws Exception
    {
        final String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":-2,\"d\":null}";
        TreeNode node = TREE_CODEC.readTree(_factory.createParser(EMPTY_READ_CONTEXT,
                INPUT));
        JsonParser p = node.traverse(EMPTY_READ_CONTEXT);

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.currentName());

        assertToken(JsonToken.START_ARRAY, p.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.currentName());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.currentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-2, p.getIntValue());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("d", p.currentName());
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        
        assertToken(JsonToken.END_OBJECT, p.nextToken());

        p.close();
    }
}
