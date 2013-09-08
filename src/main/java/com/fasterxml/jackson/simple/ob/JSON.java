package com.fasterxml.jackson.simple.ob;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.simple.ob.impl.JSONAsObjectCodec;

/**
 * Main entry point for functionality.
 *<p>
 * Note that instances are fully immutable, and thereby thread-safe.
 */
public class JSON
    implements Versioned
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

    protected final JSONReader _reader;

    protected final JSONWriter _writer;
    
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
        this(features, jsonF, trees,
                new JSONReader(DEFAULT_FEATURES),
                new JSONWriter(DEFAULT_FEATURES));
    }
    
    protected JSON(int features,
            JsonFactory jsonF, TreeCodec trees,
            JSONReader r, JSONWriter w)
    {
        _features = features;
        _jsonFactory = jsonF;
        _treeCodec = trees;
        _reader = r;
        _writer = w;
    }

    /*
    /**********************************************************************
    /* Adapting
    /**********************************************************************
     */

    /**
     * Convenience method for constructing an adapter that uses this
     * instance as a {@link ObjectCodec}
     */
    public ObjectCodec asCodec() {
        return new JSONAsObjectCodec(this);
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
        return new JSON(_features, _jsonFactory, c, _reader, _writer);
    }

    public JSON with(JSONReader r)
    {
        if (r == _reader) {
            return this;
        }
        return new JSON(_features, _jsonFactory, _treeCodec, r, _writer);
    }

    public JSON with(JSONWriter w)
    {
        if (w == _writer) {
            return this;
        }
        return new JSON(_features, _jsonFactory, _treeCodec, _reader, w);
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
        return new JSON(f, _jsonFactory, _treeCodec, _reader, _writer);
    }
    
    protected final JSON _with(JSON base,
            JsonFactory jsonF, TreeCodec trees)
    {
        if ((_jsonFactory == jsonF)
                && (_treeCodec == trees)) {
            return this;
        }
        return new JSON(base._features, jsonF, trees, _reader, _writer);
    }
    
    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    public TreeCodec getTreeCodec() {
        return _treeCodec;
    }

    public JsonFactory getJsonFactory() {
        return _jsonFactory;
    }
    
    /*
    /**********************************************************************
    /* API: writing JSON
    /**********************************************************************
     */

    public String asJSONString() throws IOException, JSONObjectException
    {
        // !!! TODO
        return null;
    }

    public byte[] asJSONBytes() throws IOException, JSONObjectException
    {
        // !!! TODO
        return null;
    }

    public void writeJSON(Object value, JsonGenerator jgen) throws IOException, JSONObjectException
    {
        // !!! TODO
    }

    public void writeJSON(Object value, OutputStream out) throws IOException, JSONObjectException
    {
        // !!! TODO
    }

    public void writeJSON(Object value, Reader r) throws IOException, JSONObjectException
    {
        // !!! TODO
    }

    public void writeJSON(Object value, File f) throws IOException, JSONObjectException
    {
        // !!! TODO
    }
    
    /*
    /**********************************************************************
    /* API: reading JSON
    /**********************************************************************
     */

    public List<Object> listFromJSON(String json) throws IOException, JSONObjectException
    {
        // !!! TODO
        return null;
    }
    
    public Map<String,Object> mapFromJSON(String json) throws IOException, JSONObjectException
    {
        // !!! TODO
        return null;
    }

    public Object fromJSON(String json) throws IOException, JSONObjectException
    {
        // !!! TODO
        return null;
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

}
