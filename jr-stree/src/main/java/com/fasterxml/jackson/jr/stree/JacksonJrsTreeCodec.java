package com.fasterxml.jackson.jr.stree;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.tree.ArrayTreeNode;
import com.fasterxml.jackson.core.tree.ObjectTreeNode;

/**
 * {@link TreeCodec} implementation that can build "simple", immutable
 * (read-only) trees out of JSON: these are represented as subtypes
 * of {@link JrsValue} ("Jrs" from "jackson JR Simple").
 */
public class JacksonJrsTreeCodec extends TreeCodec
{
    public static JrsMissing MISSING = JrsMissing.instance;

    public static final JacksonJrsTreeCodec SINGLETON = new JacksonJrsTreeCodec();

    public JacksonJrsTreeCodec() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) nodeFrom(p);
    }

    private JrsValue nodeFrom(JsonParser p) throws IOException
    {
        int tokenId = p.hasCurrentToken() ? p.currentTokenId() : p.nextToken().id();
        
        switch (tokenId) {
        case JsonTokenId.ID_TRUE:
            return JrsBoolean.TRUE;
        case JsonTokenId.ID_FALSE:
            return JrsBoolean.FALSE;
        case JsonTokenId.ID_NUMBER_INT:
        case JsonTokenId.ID_NUMBER_FLOAT:
            return new JrsNumber(p.getNumberValue());
        case JsonTokenId.ID_STRING:
            return new JrsString(p.getText());
        case JsonTokenId.ID_START_ARRAY:
            {
                List<JrsValue> values = _list();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    values.add(nodeFrom(p));
                }
                return new JrsArray(values);
            }
        case JsonTokenId.ID_START_OBJECT:
            {
                Map<String, JrsValue> values = _map();
                while (p.nextToken() != JsonToken.END_OBJECT) {
                    final String currentName = p.getCurrentName();
                    p.nextToken();
                    values.put(currentName, nodeFrom(p));
                }
                return new JrsObject(values);
            }
        case JsonTokenId.ID_EMBEDDED_OBJECT:
            // 07-Jan-2016, tatu: won't happen with JSON, but other types like Smile
            //   may produce binary data or such
            return new JrsEmbeddedObject(p.getEmbeddedObject());

        case JsonTokenId.ID_NULL:
            // 07-Jan-2016, tatu: ... or should we have NullNode too?
            return null;
        default:
        }
        throw new UnsupportedOperationException("Unsupported token id "+tokenId+" ("+p.currentToken()+")");
    }

    @Override
    public void writeTree(JsonGenerator g, TreeNode treeNode) throws IOException {
        if (treeNode == null) {
            g.writeNull();
        } else {
            ((JrsValue) treeNode).write(g, this);
        }
    }

    @Override
    public ArrayTreeNode createArrayNode() {
        return new JrsArray(_list());
    }

    @Override
    public ObjectTreeNode createObjectNode() {
        return new JrsObject(_map());
    }

    @Override
    public JsonParser treeAsTokens(TreeNode node) {
        return ((JrsValue) node).traverse(ObjectReadContext.empty());
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public TreeNode missingNode() {
        return JrsMissing.instance;
    }

    public JrsBoolean booleanNode(boolean state) {
         return state? JrsBoolean.TRUE : JrsBoolean.FALSE;
    }

    public JrsString stringNode(String text) {
        if (text == null) {
            text = "";
        }
        return new JrsString(text);
    }

    public JrsNumber numberNode(Number nr) {
        if (nr == null) {
            throw new NullPointerException();
        }
        return new JrsNumber(nr);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected List<JrsValue> _list() {
        return new ArrayList<JrsValue>();
    }

    protected Map<String,JrsValue> _map() {
        return new LinkedHashMap<String,JrsValue>();
    }
}
