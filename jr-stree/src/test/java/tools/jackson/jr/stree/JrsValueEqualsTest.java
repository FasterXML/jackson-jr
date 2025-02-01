package tools.jackson.jr.stree;

import org.junit.jupiter.api.Test;

import tools.jackson.core.TreeNode;
import tools.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.*;

public class JrsValueEqualsTest extends JacksonJrTreeTestBase
{
    private final JSON treeJSON = jsonWithTreeCodec();

    @Test
    public void testObjectEquality() throws Exception
    {
        final String INPUT = "{\"a\":1,\"b\":true,\"c\":3}";
        TreeNode tree = treeJSON.treeFrom(INPUT);
        assertEquals(tree, tree);
        assertEquals(tree, treeJSON.treeFrom(INPUT));
        assertEquals(treeJSON.treeFrom(INPUT), tree);

        final String INPUT2 = "{\"a\":1,\"b\":true}";
        TreeNode tree2 = treeJSON.treeFrom(INPUT2);
        assertEquals(tree2, tree2);

        assertFalse(tree2.equals(tree));
        assertFalse(tree.equals(tree2));
    }

    @Test
    public void testArrayEquality() throws Exception
    {
        final String INPUT = "[true,\"abc\"]";
        final TreeNode tree = treeJSON.treeFrom(INPUT);
        assertEquals(tree, tree);
        assertEquals(tree, treeJSON.treeFrom(INPUT));
        assertEquals(treeJSON.treeFrom(INPUT), tree);

        final String INPUT2 = "[false,\"abc\"]";
        final TreeNode tree2 = treeJSON.treeFrom(INPUT2);
        assertEquals(tree2, tree2);

        assertFalse(tree2.equals(tree));
        assertFalse(tree.equals(tree2));
    }

    @Test
    public void testScalarEquality() throws Exception
    {
        TreeNode tree = treeJSON.treeFrom("12");
        assertEquals(tree, treeJSON.treeFrom("12"));
        assertFalse(treeJSON.treeFrom("12").equals(treeJSON.treeFrom("-12")));

        tree = treeJSON.treeFrom("true");
        assertEquals(tree, treeJSON.treeFrom("true"));
        assertFalse(treeJSON.treeFrom("true").equals(treeJSON.treeFrom("137")));

        tree = treeJSON.treeFrom(q("name"));
        assertEquals(tree, treeJSON.treeFrom(q("name")));
        assertFalse(treeJSON.treeFrom(q("true")).equals(treeJSON.treeFrom("true")));
    }
}
