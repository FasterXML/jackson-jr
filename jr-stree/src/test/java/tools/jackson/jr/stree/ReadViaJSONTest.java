package tools.jackson.jr.stree;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for reading content using {@link JSON} with proper
 * codec registration
 */
public class ReadViaJSONTest extends JacksonJrTreeTestBase
{
    private final TreeCodec TREE_CODEC = new JacksonJrsTreeCodec();
    private final static ObjectWriteContext EMPTY_WRITE_CONTEXT = new ObjectWriteContext.Base();

    private final JSON treeJSON = jsonWithTreeCodec();

    @Test
    public void testSimpleList() throws Exception
    {
        final String INPUT = "[true,\"abc\"]";
        // and then through jr-objects:
        TreeNode node = treeJSON.treeFrom(INPUT);

        assertTrue(node instanceof JrsArray);
        assertEquals(2, node.size());
        // actually, verify with write...
        final StringWriter writer = new StringWriter();
        final JsonGenerator g = _factory.createGenerator(EMPTY_WRITE_CONTEXT,
                writer);
        TREE_CODEC.writeTree(g, node);
        g.close();
        assertEquals(INPUT, writer.toString());
    }

    @Test
     public void testSimpleMap() throws Exception
     {
         final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
         TreeNode node = treeJSON.treeFrom(INPUT);
         assertTrue(node instanceof JrsObject);
         assertEquals(3, node.size());
         // actually, verify with write...
         final StringWriter writer = new StringWriter();
         final JsonGenerator g = _factory.createGenerator(treeJSON, writer);
         TREE_CODEC.writeTree(g, node);
         g.close();
         assertEquals(INPUT, writer.toString());
     }

    @Test
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
         final JsonGenerator g = _factory.createGenerator(treeJSON, writer);
         TREE_CODEC.writeTree(g, node);
         g.close();
         assertEquals(INPUT, writer.toString());
     }
}
