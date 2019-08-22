package com.fasterxml.jackson.jr.stree;

import java.io.StringReader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.ValueIterator;

public class ReadTreeSequencesTest extends TestBase
{
    private final TreeCodec TREE_CODEC = new JacksonJrsTreeCodec();
    private final JSON treeJSON = JSON.std.with(TREE_CODEC);

    public void testBeanSequence() throws Exception
    {
        final String INPUT = aposToQuotes("{'id':1, 'msg':'foo'} [1, 2, 3] null   ");

        // First, managed
        ValueIterator<JrsValue> it = treeJSON.treeSequenceFrom(INPUT);
        _verifyTreeSequence(it);
        it.close();

        // and parser we create
        JsonParser p = treeJSON.createParser(new StringReader(INPUT));

        it = treeJSON.treeSequenceFrom(p);
        _verifyTreeSequence(it);
        it.close();
        p.close();
    }

    private void _verifyTreeSequence(ValueIterator<JrsValue> it) throws Exception
    {
        assertTrue(it.hasNext());
        JrsValue tree = it.nextValue();
        assertTrue(tree.isObject());
        assertEquals(2, tree.size());
        assertEquals(NumberType.INT, tree.path("id").numberType());
        assertEquals("foo", tree.path("msg").asText());

        assertTrue(it.hasNext());
        tree = it.nextValue();
        assertTrue(tree.isArray());
        assertEquals(3, tree.size());
        assertTrue(tree.get(0).isNumber());
        assertTrue(tree.get(1).isNumber());
        assertEquals(NumberType.INT, tree.get(2).numberType());

        assertTrue(it.hasNext());
        assertNull(it.nextValue());

        assertFalse(it.hasNext());
    }
}
