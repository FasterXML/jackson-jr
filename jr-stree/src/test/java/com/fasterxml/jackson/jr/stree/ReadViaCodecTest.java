package com.fasterxml.jackson.jr.stree;

import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.core.TreeNode;

/**
 * Tests for reading content directly using codec, and not
 * through <code>JSON</code>
 */
public class ReadViaCodecTest extends TestBase
{
    private final TreeCodec TREE_CODEC = new JacksonJrsTreeCodec();

    public void testSimpleList() throws Exception
    {
        final String INPUT = "[true,\"abc\"]";
        TreeNode node = TREE_CODEC.readTree(_factory.createParser(INPUT));

        assertTrue(node instanceof JrsArray);
        assertEquals(2, node.size());
        // actually, verify with write...
        final StringWriter writer = new StringWriter();
        final JsonGenerator g = _factory.createGenerator(writer);
        TREE_CODEC.writeTree(g, node);
        g.close();
        assertEquals(INPUT, writer.toString());
    }
    
    public void testSimpleMap() throws Exception
    {
        final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
        TreeNode node = TREE_CODEC.readTree(_factory.createParser(INPUT));
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
        TreeNode node = TREE_CODEC.readTree(_factory.createParser(INPUT));
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

    public void testSimpleJsonPointer() throws Exception
    {
        final String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
        TreeNode n;
        JrsValue v;

        TreeNode node = TREE_CODEC.readTree(_factory.createParser(INPUT));

        n = node.at("/a/1");
        assertNotNull(n);
        v = (JrsValue) n;
        assertTrue(v.isNumber());
        assertEquals(Integer.valueOf(2), ((JrsNumber) v).getValue());

        n = node.at("/a/2/b");
        assertNotNull(n);
        v = (JrsValue) n;
        assertTrue(v instanceof JrsBoolean);
        assertTrue(((JrsBoolean) v).booleanValue());

        n = node.at("/a/7");
        assertNotNull(n);
        assertTrue(n.isMissingNode());
        
        n = node.at("/a/2/c");
        assertNotNull(n);
        assertTrue(n.isMissingNode());
    }
}
