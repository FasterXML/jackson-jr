package com.fasterxml.jackson.jr.ob;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;

public class TreeApiTest extends TestBase
{
    static abstract class TestBaseNode implements TreeNode {
        @Override
        public abstract JsonToken asToken();

        public abstract void write(JsonGenerator g) throws IOException;

        @Override
        public TreeNode at(JsonPointer arg0) {
            return null;
        }

        @Override
        public TreeNode at(String arg0) throws IllegalArgumentException {
            return null;
        }

        @Override
        public Iterator<String> fieldNames() {
            return null;
        }

        @Override
        public TreeNode get(String arg0) {
            return null;
        }

        @Override
        public TreeNode get(int arg0) {
            return null;
        }

        @Override
        public boolean isContainerNode() {
            return true;
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isObject() {
            return false;
        }

        @Override
        public boolean isValueNode() {
            return false;
        }

        @Override
        public NumberType numberType() {
            return null;
        }

        @Override
        public TreeNode path(String arg0) {
            return null;
        }

        @Override
        public TreeNode path(int arg0) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public JsonParser traverse() {
            return null;
        }

        @Override
        public JsonParser traverse(ObjectCodec arg0) {
            return null;
        }
    }

    static class TestArrayNode extends TestBaseNode {
        @Override
        public JsonToken asToken() { return JsonToken.START_ARRAY; }

        @Override
        public boolean isArray() { return true; }

        @Override
        public void write(JsonGenerator g) throws IOException {
            g.writeStartArray();
            g.writeEndArray();
        }
    }

    static class TestObjectNode extends TestBaseNode {
        @Override
        public JsonToken asToken() { return JsonToken.START_OBJECT; }

        @Override
        public boolean isObject() { return true; }

        @Override
        public void write(JsonGenerator g) throws IOException {
            g.writeStartObject();
            g.writeEndObject();
        }
    }

    static class TestTreeCodec extends TreeCodec
    {
        @Override
        public TreeNode createArrayNode() {
            return new TestArrayNode();
        }

        @Override
        public TreeNode createObjectNode() {
            return new TestObjectNode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
            if (p.isExpectedStartArrayToken()) {
                return (T) createArrayNode();
            }
            if (p.isExpectedStartArrayToken()) {
                return (T) createObjectNode();
            }
            throw new IllegalStateException("Can't do: "+p.currentToken());
        }

        @Override
        public JsonParser treeAsTokens(TreeNode arg0) {
            throw new IllegalStateException("No treeAsTokens() implemented");
        }

        @Override
        public void writeTree(JsonGenerator g, TreeNode tree) throws IOException {
            g.writeTree(tree);
        }
    }

    @SuppressWarnings("deprecation")
    private final JSON J = JSON.builder()
            .treeCodec(new TestTreeCodec())
            .build();

    public void testSimpleNodeCreation() {
        assertEquals(TestArrayNode.class, J.createArrayNode().getClass());
        assertEquals(TestObjectNode.class, J.createObjectNode().getClass());
    }
}
