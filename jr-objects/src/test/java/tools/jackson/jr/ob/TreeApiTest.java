package tools.jackson.jr.ob;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import tools.jackson.core.*;
import tools.jackson.core.JsonParser.NumberType;
import tools.jackson.core.tree.ArrayTreeNode;
import tools.jackson.core.tree.ObjectTreeNode;

public class TreeApiTest extends TestBase
{
    static abstract class TestBaseNode implements TreeNode {
        @Override
        public abstract JsonToken asToken();

        public abstract void write(JsonGenerator g);

        @Override
        public TreeNode at(JsonPointer arg0) {
            return null;
        }

        @Override
        public TreeNode at(String arg0) throws IllegalArgumentException {
            return null;
        }

        @Override
        public Collection<String> propertyNames() {
            return Collections.emptyList();
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
        public boolean isNull() {
            return false;
        }

        @Override
        public boolean isEmbeddedValue() {
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
        public JsonParser traverse(ObjectReadContext arg0) {
            return null;
        }
    }

    static class TestArrayNode extends TestBaseNode
        implements ArrayTreeNode
    {
        @Override
        public JsonToken asToken() { return JsonToken.START_ARRAY; }

        @Override
        public boolean isArray() { return true; }

        @Override
        public void write(JsonGenerator g) {
            g.writeStartArray();
            g.writeEndArray();
        }
    }

    static class TestObjectNode extends TestBaseNode
        implements ObjectTreeNode
    {
        @Override
        public JsonToken asToken() { return JsonToken.START_OBJECT; }

        @Override
        public boolean isObject() { return true; }

        @Override
        public void write(JsonGenerator g) {
            g.writeStartObject();
            g.writeEndObject();
        }
    }

    static class TestTreeCodec implements TreeCodec
    {
        @Override
        public ArrayTreeNode createArrayNode() {
            return new TestArrayNode();
        }

        @Override
        public ObjectTreeNode createObjectNode() {
            return new TestObjectNode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TreeNode> T readTree(JsonParser p) {
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
        public void writeTree(JsonGenerator g, TreeNode tree) {
            g.writeTree(tree);
        }

        @Override
        public TreeNode booleanNode(boolean b) {
            return null;
        }

        @Override
        public TreeNode stringNode(String text) {
            return null;
        }

        @Override
        public TreeNode missingNode() {
            return null;
        }

        @Override
        public TreeNode nullNode() {
            return null;
        }
    }

    private final JSON J = JSON.builder()
            .treeCodec(new TestTreeCodec())
            .build();

    public void testSimpleNodeCreation() {
        assertEquals(TestArrayNode.class, J.createArrayNode().getClass());
        assertEquals(TestObjectNode.class, J.createObjectNode().getClass());
    }
}
