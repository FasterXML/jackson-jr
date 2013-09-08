package com.fasterxml.jackson.simple.ob.impl;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.simple.ob.JSON;

/**
 * Convenience wrapper around {@link JSON} that implements {@link ObjectCodec}
 */
public class JSONAsObjectCodec
    extends ObjectCodec
{
    protected final JSON _json;

    protected final JsonFactory _jsonFactory;
    
    protected final TreeCodec _treeCodec;
    
    public JSONAsObjectCodec(JSON json) {
        this(json, json.getJsonFactory());
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

    /*
    /**********************************************************************
    /* ObjectCodec: Object reads
    /**********************************************************************
     */
    
    @Override
    public <T> T readValue(JsonParser jp, Class<T> valueType)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T readValue(JsonParser jp, ResolvedType valueType)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser jp, Class<T> valueType)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser jp,
            TypeReference<?> valueTypeRef) throws IOException,
            JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser jp, ResolvedType valueType)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
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
        // Let's handle trivial case first, to simplify further processing
        if (value == null) {
            jgen.writeNull();
            return;
        }
        Class<?> rawType = value.getClass();

        // TODO Auto-generated method stub
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
    public void writeTree(JsonGenerator jg, TreeNode tree) throws IOException, JsonProcessingException
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
         * we should be able to do that.
         */
        
        
//        return _checkTreeCodec().treeToValue(n, valueType);
        return null;
    }
    
    /*
    /**********************************************************************
    /* ObjectCodec: other
    /**********************************************************************
     */

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
}
