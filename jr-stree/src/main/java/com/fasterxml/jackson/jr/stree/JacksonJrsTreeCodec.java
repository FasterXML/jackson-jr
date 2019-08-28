package com.fasterxml.jackson.jr.stree;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;

/**
 * {@link TreeCodec} implementation that can build "simple", immutable
 * (read-only) trees out of JSON: these are represented as subtypes
 * of {@link JrsValue} ("Jrs" from "jackson JR Simple").
 */
public class JacksonJrsTreeCodec extends TreeCodec
{
    public static JrsMissing MISSING = JrsMissing.instance;

    public static final JacksonJrsTreeCodec SINGLETON = new JacksonJrsTreeCodec();

    protected ObjectCodec _objectCodec;

    public JacksonJrsTreeCodec() {
        this(null);
    }

    public JacksonJrsTreeCodec(ObjectCodec codec) {
        _objectCodec = codec;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) nodeFrom(p);
    }

    private JrsValue nodeFrom(JsonParser p) throws IOException
    {
        int tokenId = p.hasCurrentToken()
                ? p.getCurrentTokenId() : p.nextToken().id();
        
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
            return JrsNull.instance;
        default:
        }
        throw new UnsupportedOperationException("Unsupported token id "+tokenId+" ("+p.getCurrentToken()+")");
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
    public JrsValue createArrayNode() {
        return new JrsArray(_list());
    }

    @Override
    public JrsValue createObjectNode() {
        return new JrsObject(_map());
    }

    @Override
    public JrsValue missingNode() {
        return JrsMissing.instance();
    }

    @Override
    public JrsValue nullNode() {
        return JrsNull.instance();
    }

    @Override
    public JsonParser treeAsTokens(TreeNode node) {
        return ((JrsValue) node).traverse(_objectCodec);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * @since 2.8
     */
    public JrsBoolean booleanNode(boolean state) {
         return state? JrsBoolean.TRUE : JrsBoolean.FALSE;
    }

    /**
     * @since 2.8
     */
    public JrsString stringNode(String text) {
        if (text == null) {
            text = "";
        }
        return new JrsString(text);
    }

    /**
     * @since 2.8
     */
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
