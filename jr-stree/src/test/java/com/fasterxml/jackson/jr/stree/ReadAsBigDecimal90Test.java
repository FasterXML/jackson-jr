package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;

import java.math.BigDecimal;

public class ReadAsBigDecimal90Test extends JacksonJrTreeTestBase
{
    // [jackson-jr#90]
    public void testReadAsBigDecimal() throws Exception
    {
        JSON json = JSON.builder()
                .enable(JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS)
                .register(new JrSimpleTreeExtension())
                .build();

        String input = "[1.1]";

        TreeNode node = json.treeFrom(input);
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(BigDecimal.class,
                ((JrsNumber) elemNode).getValue().getClass());
    }

    public void testDefaultBehaviourWithBigDecimalFlag() throws Exception
    {
        JSON json = JSON.builder()
                .register(new JrSimpleTreeExtension())
                .build();

        String input = "[1.1]";

        TreeNode node = json.treeFrom(input);
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(Double.class,
                ((JrsNumber) elemNode).getValue().getClass());
    }
}
