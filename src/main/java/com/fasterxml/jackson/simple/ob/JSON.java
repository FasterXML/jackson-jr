package com.fasterxml.jackson.simple.ob;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Main entry point for functionality.
 *<p>
 * Note that instances are fully immutable, and thereby thread-safe.
 */
public class JSON
    extends ObjectCodec
{
    /**
     * Singleton instance with standard, default configuration.
     * May be used with direct references like:
     *<pre>
     *   String json = JSON.std.asString(map);
     *</pre>
     */
    public final static JSON std = new JSON();

    private final static int DEFAULT_FEATURES = Feature.defaults();
    
    /*
    /**********************************************************************
    /* Configuration, helper objects
    /**********************************************************************
     */

    /**
     * Underlying JSON factory used for creating Streaming parsers and
     * generators.
     */
    protected final JsonFactory _jsonFactory;

    /**
     * Optional handler for {@link TreeNode} values: if defined, we can
     * read and write {@link TreeNode} instances that codec supports.
     */
    protected final TreeCodec _treeCodec;
    
    /*
    /**********************************************************************
    /* Configuration, simple settings
    /**********************************************************************
     */

    protected final int _features;
    
    /*
    /**********************************************************************
    /* Basic construction
    /**********************************************************************
     */
    
    public JSON() {
        this(DEFAULT_FEATURES, new JsonFactory(), null);
    }

    protected JSON(int features,
            JsonFactory jsonF, TreeCodec trees)
    {
        _features = features;
        _jsonFactory = jsonF;
        _treeCodec = trees;
    }
    
    /*
    /**********************************************************************
    /* Versioned
    /**********************************************************************
     */
    
    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************************
    /* Mutant factories
    /**********************************************************************
     */

    public JSON with(JsonFactory f)
    {
        if (f == _jsonFactory) {
            return this;
        }
        return new JSON(_features, f, _treeCodec);
    }

    public JSON with(TreeCodec c)
    {
        if (c == _treeCodec) {
            return this;
        }
        return new JSON(_features, _jsonFactory, c);
    }

    public JSON with(Feature feature, boolean state)
    {
        int f = _features;
        if (state) {
            f |= feature.mask();
        } else {
            f &= ~feature.mask();
        }
        return _with(f);
    }
    
    public JSON with(Feature ... features)
    {
        int flags = _features;
        for (Feature feature : features) {
            flags |= feature.mask();
        }
        return _with(flags);
    }

    public JSON without(Feature ... features)
    {
        int flags = _features;
        for (Feature feature : features) {
            flags &= ~feature.mask();
        }
        return _with(flags);
    }

    protected final JSON _with(int f) {
        if (_features == f) {
            return this;
        }
        return new JSON(f, _jsonFactory, _treeCodec);
    }
    
    protected final JSON _with(JSON base,
            JsonFactory jsonF, TreeCodec trees)
    {
        if ((_jsonFactory == jsonF)
                && (_treeCodec == trees)) {
            return this;
        }
        return new JSON(base._features, jsonF, trees);
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

    /**
     * Use of this method is discouraged, because it can be used to potentially
     * cause thread-safety problems if modifications are made during operation;
     * the reason for inclusion is that it is part of {@link ObjectCodec} API
     * that we implement.
     * Use it <b>ONLY IF YOU KNOW WHAT YOU ARE DOING</b>.
     */
    @Deprecated
    @Override
    public JsonFactory getJsonFactory() {
        return _jsonFactory;
    }
    
    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

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
