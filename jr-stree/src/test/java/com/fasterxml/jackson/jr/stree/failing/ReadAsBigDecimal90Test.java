package com.fasterxml.jackson.jr.stree.failing;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.TreeNode;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.stree.*;

public class ReadAsBigDecimal90Test extends JacksonJrTreeTestBase
{
    // [jackson-jr#90]
    public void testReadAsBigDecimal() throws Exception
    {
        JSON json = JSON.builder()
                .treeCodec(new JacksonJrsTreeCodec())
                .enable(JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS)
                .build();

        String input = "[1.1]";

        TreeNode node = json.treeFrom(input);
        TreeNode elemNode = node.get(0);

        assertTrue(elemNode.isValueNode());
        assertTrue(elemNode instanceof JrsNumber);
        assertEquals(BigDecimal.class,
                ((JrsNumber) elemNode).getValue().getClass());
    }
}
