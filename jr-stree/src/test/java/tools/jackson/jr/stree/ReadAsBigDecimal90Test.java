package tools.jackson.jr.stree;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import tools.jackson.core.TreeNode;
import tools.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.*;

// [jackson-jr#90]: JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS should work
public class ReadAsBigDecimal90Test extends JacksonJrTreeTestBase
{
    // [jackson-jr#90]
    @Test
    public void testDefaultBehaviourReadAsDouble() throws Exception
    {
        JSON json = JSON.builder()
                .disable(JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS)
                .register(new JrSimpleTreeExtension())
                .build();
        TreeNode node = json.treeFrom("[1.1]");
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(Double.class,
                ((JrsNumber) elemNode).getValue().getClass());

        _verifyInt(json);
    }

    // [jackson-jr#90]
    @Test
    public void testReadAsBigDecimal() throws Exception
    {
        JSON json = JSON.builder()
                .enable(JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS)
                .register(new JrSimpleTreeExtension())
                .build();
        TreeNode node = json.treeFrom("[1.1]");
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(BigDecimal.class,
                ((JrsNumber) elemNode).getValue().getClass());

        _verifyInt(json);
    }

    private void _verifyInt(JSON json) throws Exception
    {
        TreeNode node = json.treeFrom("[123]");
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(Integer.class,
                ((JrsNumber) elemNode).getValue().getClass());
        assertEquals(123, ((JrsNumber) elemNode).getValue().intValue());
    }
}
