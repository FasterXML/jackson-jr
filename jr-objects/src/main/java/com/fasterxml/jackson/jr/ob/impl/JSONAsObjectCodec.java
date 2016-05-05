package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.PackageVersion;

/**
 * Convenience wrapper around {@link JSON} that implements {@link ObjectCodec}.
 * Note that implementation is not complete, due to natural limitations of
 * {@link JSON} and "simple" object binding.
 *<p>
 * The main use case is to give minimal context for other components that
 * expect to get a {@link ObjectCodec}, such as {@link JsonParser} and
 * {@link JsonGenerator}.
 */
public class JSONAsObjectCodec
    extends ObjectCodec
{
    protected final JSON _json;

    protected final JsonFactory _jsonFactory;
    
    protected final TreeCodec _treeCodec;
    
    public JSONAsObjectCodec(JSON json) {
        this(json, json.getStreamingFactory());
    }

    public JSONAsObjectCodec(JSON json, JsonFactory jf)
    {
        this(json, jf, json.getTreeCodec());
    }
    
    public JSONAsObjectCodec(JSON json, JsonFactory jf, TreeCodec treeCodec)
    {
        _json = json;
        _jsonFactory = jf;
        _treeCodec = treeCodec;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************************
    /* ObjectCodec: Object reads
    /**********************************************************************
     */
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T readValue(JsonParser jp, Class<T> valueType)
            throws IOException, JsonProcessingException
    {
        Object ob = _json.anyFrom(jp);
        _checkResultType(valueType, ob);
        return (T) ob;
    }
    
    @Override
    public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef)
            throws IOException, JsonProcessingException
    {
        throw _noTypeReference();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readValue(JsonParser jp, ResolvedType valueType)
            throws IOException, JsonProcessingException {
        return (T) readValue(jp, valueType.getRawClass());
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser jp, Class<T> valueType)
            throws IOException, JsonProcessingException {
        // May be able to support in future but...
        throw new JSONObjectException("Simple JSON does not support 'readValues()' methods");
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser jp,
        TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException
    {
        throw _noTypeReference();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Iterator<T> readValues(JsonParser jp, ResolvedType valueType)
            throws IOException, JsonProcessingException {
        return (Iterator<T>) readValues(jp, valueType.getRawClass());
    }
    
    protected JSONObjectException _noTypeReference() {
        return new JSONObjectException("Simple JSON does not support use of TypeReference");
    }

    /*
    /**********************************************************************
    /* ObjectCodec: Object writes
    /**********************************************************************
     */

    @Override
    public void writeValue(JsonGenerator jgen, Object value)
            throws IOException, JsonProcessingException
    {
        _json.write(value, jgen);
    }
    /*
    /**********************************************************************
    /* ObjectCodec: Tree
    /**********************************************************************
     */

    @Override
    public TreeNode createObjectNode() {
        return _checkTreeCodec().createObjectNode();
    }

    @Override
    public TreeNode createArrayNode() {
        return _checkTreeCodec().createArrayNode();
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser jp) throws IOException, JsonProcessingException
    {
        return _checkTreeCodec().readTree(jp);
    }

    @Override
    public void writeTree(JsonGenerator jg, TreeNode tree)
        throws IOException, JsonProcessingException
    {
        _checkTreeCodec().writeTree(jg, tree);
    }
    
    @Override
    public JsonParser treeAsTokens(TreeNode n) {
        return _checkTreeCodec().treeAsTokens(n);
    }

    @Override
    public <T> T treeToValue(TreeNode n, Class<T> valueType)
        throws JsonProcessingException
    {
        /* Without TokenBuffer from jackson-databind, need to actually
         * create an intermediate textual representation. Fine,
         * we should be able to do that. Bigger question is whether
         * actual read works but...
         */
        try {
            String json = _json.asString(n);
            JsonParser jp = _jsonFactory.createParser(json);
            T result = readValue(jp, valueType);
            jp.close();
            return result;
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
    }
    
    /*
    /**********************************************************************
    /* ObjectCodec: other
    /**********************************************************************
     */

    @Override
    public JsonFactory getFactory() {
        return _jsonFactory;
    }

    @Deprecated
    @Override
    public JsonFactory getJsonFactory() {
        return _jsonFactory;
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected TreeCodec _checkTreeCodec()
    {
        TreeCodec c = _treeCodec;
        if (c == null) {
            throw new IllegalStateException("No TreeCodec has been configured: can not use tree operations");
        }
        return c;
    }

    protected void _checkResultType(Class<?> valueType, Object ob)
        throws JSONObjectException
    {
        if (ob != null) {
            if (!valueType.isAssignableFrom(ob.getClass())) {
                throw new JSONObjectException("Simple JSON can only bind given JSON as "
                        +ob.getClass().getName()+", not as "+valueType.getName());
            }
        }
    }
}
