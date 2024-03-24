package com.fasterxml.jackson.jr.stree;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * {@link TreeCodec} implementation that can build "simple", immutable
 * (read-only) trees out of JSON: these are represented as subtypes
 * of {@link JrsValue} ("Jrs" from "jackson JR Simple").
 */
public class JacksonJrsTreeCodec extends TreeCodec
{
    public static final JrsMissing MISSING = JrsMissing.instance;

    protected final ObjectCodec _objectCodec;

    // @since 2.17
    protected boolean _failOnDuplicateKeys;

    // @since 2.17.1
    protected boolean _useBigDecimalForDouble;

    public JacksonJrsTreeCodec() {
        this(null);
    }

    public JacksonJrsTreeCodec(ObjectCodec codec) {
        _objectCodec = codec;
    }

    // @since 2.17
    public void setFailOnDuplicateKeys(boolean state) {
        _failOnDuplicateKeys = state;
    }

    // @since 2.17.1
    public void setUseBigDecimalForDouble(boolean state) {
        _useBigDecimalForDouble = state;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) nodeFrom(p);
    }

    private JrsValue nodeFrom(JsonParser p) throws IOException
    {
        int tokenId = p.hasCurrentToken()
                ? p.currentTokenId() : p.nextToken().id();

        switch (tokenId) {
        case JsonTokenId.ID_TRUE:
            return JrsBoolean.TRUE;
        case JsonTokenId.ID_FALSE:
            return JrsBoolean.FALSE;
        case JsonTokenId.ID_NUMBER_INT:
            // Important! No coercion to BigDecimal (wrt [jackson-jr#90]
            return new JrsNumber(p.getNumberValue());
        case JsonTokenId.ID_NUMBER_FLOAT:
            if (_useBigDecimalForDouble) {
                return new JrsNumber(p.getDecimalValue());
            }
            return new JrsNumber(p.getNumberValue());
        case JsonTokenId.ID_STRING:
            return new JrsString(p.getText());
        case JsonTokenId.ID_START_ARRAY: {
            List<JrsValue> values = _list();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                values.add(nodeFrom(p));
            }
            return new JrsArray(values);
        }
        case JsonTokenId.ID_START_OBJECT: {
            Map<String, JrsValue> values = _map();
            while (p.nextToken() != JsonToken.END_OBJECT) {
                final String currentName = p.currentName();
                p.nextToken();
                JrsValue prev = values.put(currentName, nodeFrom(p));
                if (_failOnDuplicateKeys && (prev != null)) {
                    throw new JSONObjectException("Duplicate key (key '" + currentName + "')");
                }
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
        throw new UnsupportedOperationException("Unsupported token id " + tokenId + " (" + p.currentToken() + ")");
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
        return node.traverse(_objectCodec);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Factory method for constructing node to represent Boolean values.
     *
     * @param state Whether to create {@code Boolean.TRUE} or {@code Boolean.FALSE} node
     * @return Node instance for given boolean value
     *
     * @since 2.8
     */
    public JrsBoolean booleanNode(boolean state) {
        return state ? JrsBoolean.TRUE : JrsBoolean.FALSE;
    }

    /**
     * Factory method for constructing node to represent String values.
     *
     * @param text String value for constructed node to contain
     * @return Node instance for given text value
     *
     * @since 2.8
     */
    public JrsString stringNode(String text) {
        if (text == null) {
            text = "";
        }
        return new JrsString(text);
    }

    /**
     * Factory method for constructing node to represent String values.
     *
     * @param nr Numeric value for constructed node to contain
     * @return Node instance for given numeric value
     *
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
        return new ArrayList<>();
    }

    protected Map<String, JrsValue> _map() {
        return new LinkedHashMap<>();
    }
}
