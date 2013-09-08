package com.fasterxml.jackson.simple.ob;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.Instantiatable;
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

    protected final PrettyPrinter _prettyPrinter;
    
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
                new JSONWriter(DEFAULT_FEATURES),
                null);
    }
    
    protected JSON(int features,
            JsonFactory jsonF, TreeCodec trees,
            JSONReader r, JSONWriter w,
            PrettyPrinter pp)
    {
        _features = features;
        _jsonFactory = jsonF;
        _treeCodec = trees;
        _reader = r;
        _writer = w;
        _prettyPrinter = pp;
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
        return new JSON(_features, _jsonFactory, c,
                _reader, _writer, _prettyPrinter);
    }

    public JSON with(JSONReader r)
    {
        if (r == _reader) {
            return this;
        }
        return new JSON(_features, _jsonFactory, _treeCodec,
                r, _writer, _prettyPrinter);
    }

    public JSON with(JSONWriter w)
    {
        if (w == _writer) {
            return this;
        }
        return new JSON(_features, _jsonFactory, _treeCodec,
                _reader, w, _prettyPrinter);
    }

    public JSON with(PrettyPrinter pp)
    {
        if (_prettyPrinter == pp) {
            return this;
        }
        return new JSON(_features, _jsonFactory, _treeCodec,
                _reader, _writer, pp);
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
        return new JSON(f, _jsonFactory, _treeCodec,
                _reader, _writer, _prettyPrinter);
    }
    
    protected final JSON _with(JSON base,
            JsonFactory jsonF, TreeCodec trees)
    {
        if ((_jsonFactory == jsonF)
                && (_treeCodec == trees)) {
            return this;
        }
        return new JSON(base._features, jsonF, trees,
                _reader, _writer, _prettyPrinter);
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

    public final boolean isEnabled(Feature f) {
        return (f.mask() & _features) != 0;
    }
    
    /*
    /**********************************************************************
    /* API: writing JSON
    /**********************************************************************
     */

    @SuppressWarnings("resource")
    public String asJSONString(Object value) throws IOException, JSONObjectException
    {
        SegmentedStringWriter sw = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _jsonFactory.createGenerator(sw));
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    @SuppressWarnings("resource")
    public byte[] asJSONBytes(Object value) throws IOException, JSONObjectException
    {
        ByteArrayBuilder bb = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _jsonFactory.createGenerator(bb, JsonEncoding.UTF8));
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
        byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }

    public void writeJSON(Object value, JsonGenerator jgen) throws IOException, JSONObjectException
    {
        // NOTE: no call to _config(); assumed to be fully configured
        _writer(jgen).writeValue(value);
        if (isEnabled(Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }

    public void writeJSON(Object value, OutputStream out)
        throws IOException, JSONObjectException
    {
        _writeAndClose(value, _jsonFactory.createGenerator(out));
    }

    public void writeJSON(Object value, Writer w) throws IOException, JSONObjectException
    {
        _writeAndClose(value, _jsonFactory.createGenerator(w));
    }

    public void writeJSON(Object value, File f) throws IOException, JSONObjectException
    {
        _writeAndClose(value, _jsonFactory.createGenerator(f, JsonEncoding.UTF8));
    }
    
    /*
    /**********************************************************************
    /* API: reading JSON
    /**********************************************************************
     */

    public List<Object> listFromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        List<Object> result;
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            jp = _initForReading((JsonParser) source);
            result = _reader(jp).readList();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _reader(jp).readList();
                closed = true;
            } finally {
                if (!closed) {
                    _close(jp);
                }
            }
        }
        // Need to consume the token too
        jp.clearCurrentToken();
        return result;
    }

    public Map<String,Object> mapFromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        Map<String,Object> result;
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            jp = _initForReading((JsonParser) source);
            result = _reader(jp).readMap();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _reader(jp).readMap();
                closed = true;
            } finally {
                if (!closed) {
                    _close(jp);
                }
            }
        }
        // Need to consume the token too
        jp.clearCurrentToken();
        return result;
    }

    public Object fromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        Object result;
        if (source instanceof JsonParser) {
            jp = _initForReading((JsonParser) source);
            result = _reader(jp).readValue();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _reader(jp).readValue();
                closed = true;
            } finally {
                if (!closed) {
                    _close(jp);
                }
            }
        }
        jp.clearCurrentToken();
        return result;
    }
    
    /*
    /**********************************************************************
    /* Internal methods, writing
    /**********************************************************************
     */

    protected final void _writeAndClose(Object value, JsonGenerator jgen)
        throws IOException, JSONObjectException
    {
        boolean closed = false;
        try {
            _config(jgen);
            _writer(jgen).writeValue(value);
            closed = true;
            jgen.close();
        } finally {
            if (!closed) {
                // need to catch possible failure, so as not to mask problem
                try {
                    jgen.close();
                } catch (IOException ioe) { }
            }
        }
    }

    protected JSONWriter _writer(JsonGenerator jgen) {
        return _writer.newWriter(jgen);
    }

    /*
    /**********************************************************************
    /* Internal methods, reading
    /**********************************************************************
     */
    
    protected JSONReader _reader(JsonParser jp) {
        return _reader.newReader(jp);
    }

    protected JsonParser _parser(Object source) throws IOException, JSONObjectException
    {
        final JsonFactory f = _jsonFactory;
        final Class<?> type = source.getClass();
        if (type == String.class) {
            return f.createParser((String) source);
        }
        if (source instanceof InputStream) {
            return f.createParser((InputStream) source);
        }
        if (source instanceof Reader) {
            return f.createParser((Reader) source);
        }
        if (type == byte[].class) {
            return f.createParser((byte[]) source);
        }
        if (source instanceof URL) {
            return f.createParser((URL) source);
        }
        if (type == char[].class) {
            return f.createParser(new CharArrayReader((char[]) source));
        }
        if (source instanceof CharSequence) {
            return f.createParser(((CharSequence) source).toString());
        }
        if (source instanceof JsonParser) { // should never be called with this
            throw new IllegalStateException();
        }
        throw new JSONObjectException("Can not use Source of type "+source.getClass().getName()
                +" as input (use an InputStream, Reader, String, byte[], File or URL");
    }

    protected JsonParser _initForReading(JsonParser jp)
        throws IOException, JsonProcessingException
    {
        /* First: must point to a token; if not pointing to one, advance.
         * This occurs before first read from JsonParser, as well as
         * after clearing of current token.
         */
        JsonToken t = jp.getCurrentToken();
        if (t == null) { // and then we must get something...
            t = jp.nextToken();
            if (t == null) { // not cool is it?
                throw JSONObjectException.from(jp, "No content to map due to end-of-input");
            }
        }
        return jp;
    }
    
    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    protected JsonGenerator _config(JsonGenerator jgen)
    {
        // First, possible pretty printing
        PrettyPrinter pp = _prettyPrinter;
        if (pp != null) {
            if (pp instanceof Instantiatable<?>) {
                pp = (PrettyPrinter) ((Instantiatable<?>) pp).createInstance();
            }
            jgen.setPrettyPrinter(pp);
        } else if (this.isEnabled(Feature.PRETTY_PRINT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        return jgen;
    }

    protected JsonParser _config(JsonParser jp)
    {
        // nothing to do, yet
    }

    protected void _close(Closeable cl) {
        try {
            cl.close();
        } catch (IOException ioe) { }
    }
}
