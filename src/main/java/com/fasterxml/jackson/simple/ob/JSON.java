package com.fasterxml.jackson.simple.ob;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.Instantiatable;
import com.fasterxml.jackson.simple.ob.impl.*;

/**
 * Main entry point for functionality.
 *<p>
 * Note that instances are fully immutable, and thereby thread-safe.
 */
public class JSON
    implements Versioned
{
    /**
     * Simple on/off (enabled/disabled) features for {@link JSON}; used for simple configuration
     * aspects.
     */
    public enum Feature
    {
       /*
       /**********************************************************************
       /* Read-related features
       /**********************************************************************
        */

       /**
        * When reading JSON Numbers, should {@link java.math.BigDecimal} be used
        * for floating-point numbers; or should {@link java.lang.Double} be used.
        * Trade-off is between accuracy -- only {@link java.math.BigDecimal} is
        * guaranteed to store the EXACT decimal value parsed -- and performance
        * ({@link java.lang.Double} is typically faster to parser).
        *<p>
        * Default setting is <code>false</code>, meaning that {@link java.lang.Double}
        * is used.
        */
       USE_BIG_DECIMAL_FOR_FLOATS(false),

       /**
        * When reading JSON Arrays, should matching Java value be of type
        * <code>Object[]</code> (true) or {@link java.util.List} (false)?
        *<p>
        * Default setting is <code>false</code>, meaning that JSON Arrays
        * are bound to {@link java.util.List}s.
        */
       READ_JSON_ARRAYS_AS_JAVA_ARRAYS(false),

       /**
        * This feature can be enabled to reduce memory usage for use cases where
        * resulting container objects ({@link java.util.Map}s and {@link java.util.Collection}s)
        * do not need to mutable (that is, their contents can not changed).
        * If set, reader is allowed to construct immutable (read-only)
        * container objects; and specifically empty {@link java.util.Map}s and
        * {@link java.util.Collection}s can be used to reduce number of
        * objects allocated. In addition, sizes of non-empty containers can
        * be trimmed to exact size.
        *<p>
        * Default setting is <code>false</code>, meaning that reader will have to
        * construct mutable container instance when reading.
        */
       READ_ONLY(false),

       /**
        * This feature can be used to indicate that the reader should preserve
        * order of the properties same as what input document has.
        * Note that it is up to {@link com.fasterxml.jackson.simple.ob.impl.MapBuilder}
        * to support this feature; custom implementations may ignore the setting.
        *<p>
        * Default setting is <code>true</code>, meaning that reader is expected to try to
        * preserve ordering of fields read.
        */
       PRESERVE_FIELD_ORDERING(true),
       
       /**
        * When encountering duplicate keys for JSON Objects, should an exception
        * be thrown or not? If exception is not thrown, <b>the last</b> instance
        * from input document will be used.
        *<p>
        * Default setting is <code>true</code>, meaning that a
        * {@link JSONObjectException} will be thrown if duplicates are encountered.
        */
       FAIL_ON_DUPLICATE_MAP_KEYS(true),
       
       /*
       /**********************************************************************
       /* Write-related features
       /**********************************************************************
        */

       /**
        * Feature that can be enabled to use "pretty-printing", basic indentation
        * to make resulting JSON easier to read by humans by adding white space
        * such as line feeds and indentation.
        *<p>
        * Default setting is <code>false</code> so that no pretty-printing is done
        * (unless explicitly constructed with a pretty printer object)
        */
       PRETTY_PRINT_OUTPUT(false),
       
       /**
        * Feature that determines whether <code>JsonGenerator.flush()</code> is
        * called after <code>writeJSON()</code> method <b>that takes JsonGenerator
        * as an argument</b> completes (that is, does NOT affect methods
        * that use other destinations).
        * This usually makes sense; but there are cases where flushing
        * should not be forced: for example when underlying stream is
        * compressing and flush() causes compression state to be flushed
        * (which occurs with some compression codecs).
        *<p>
        * Feature is enabled by default.
        */
       FLUSH_AFTER_WRITE_VALUE(true),
       
       ;

       /*
       /**********************************************************************
       /* Enum impl
       /**********************************************************************
        */

       private final boolean _defaultState;

       private final int _mask;
       
       private Feature(boolean defaultState) {
           _defaultState = defaultState;
           _mask = (1 << ordinal());
       }

       public static int defaults()
       {
           int flags = 0;
           for (Feature value : values()) {
               if (value.enabledByDefault()) {
                   flags |= value.mask();
               }
           }
           return flags;
       }
       
       public boolean enabledByDefault() { return _defaultState; }

       public int mask() { return _mask; }

       public boolean isEnabled(int flags) {
           return (flags & _mask) != 0;
       }
   }
    
    // Important: has to come before 'std' instance, since it refers to it
    private final static int DEFAULT_FEATURES = Feature.defaults();

    /**
     * Singleton instance with standard, default configuration.
     * May be used with direct references like:
     *<pre>
     *   String json = JSON.std.asString(map);
     *</pre>
     */
    public final static JSON std = new JSON();
    
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
    
    /**
     * Blueprint instance of the reader to use for reading JSON as simple
     * Objects.
     */
    protected final JSONReader _reader;

    /**
     * Blueprint isntance of the writer to use for writing JSON given
     * simple Objects.
     */
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
                null, null, // reader, writer
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
        _reader = (r == null) ? _defaultReader(features) : r;
        _writer = (w == null) ? _defaultWriter(features, trees) : w;
        _prettyPrinter = pp;
    }

    protected JSONReader _defaultReader(int features) {
        return new JSONReader(features, ListBuilder.defaultImpl(), MapBuilder.defaultImpl());
    }

    protected JSONWriter _defaultWriter(int features, TreeCodec tc) {
        return new JSONWriter(features, TypeDetector.rootDetector(), tc);
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
        return _with(_features, f, _treeCodec, _reader, _writer, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link TreeCodec},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(TreeCodec c)
    {
        if (c == _treeCodec) {
            return this;
        }
        return _with(_features, _jsonFactory, c,
                _reader, _writer.with(c), _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link JSONReader},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(JSONReader r)
    {
        if (r == _reader) {
            return this;
        }
        return _with(_features, _jsonFactory, _treeCodec,
                r, _writer, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link JSONWriter},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(JSONWriter w)
    {
        if (w == _writer) {
            return this;
        }
        return _with( _features, _jsonFactory, _treeCodec,
                _reader, w, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link PrettyPrinter},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(PrettyPrinter pp)
    {
        if (_prettyPrinter == pp) {
            return this;
        }
        return _with(_features, _jsonFactory, _treeCodec,
                _reader, _writer, pp);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link MapBuilder},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(MapBuilder b) {
        JSONReader r = _reader.with(b);
        if (r == _reader) {
            return this;
        }
        return _with(_features, _jsonFactory, _treeCodec,
                r, _writer, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link ListBuilder},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(ListBuilder b) {
        JSONReader r = _reader.with(b);
        if (r == _reader) {
            return this;
        }
        return _with(_features, _jsonFactory, _treeCodec,
                r, _writer, _prettyPrinter);
    }
    
    /**
     * Mutant factory for constructing an instance with specified feature
     * enabled or disabled (depending on <code>state</codec>), and returning
     * an instance with that setting; this may either be this instance (if feature
     * already had specified state), or a newly constructed instance.
     */
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
    
    /**
     * Mutant factory for constructing an instance with specified features
     * enabled.
     */
    public JSON with(Feature ... features)
    {
        int flags = _features;
        for (Feature feature : features) {
            flags |= feature.mask();
        }
        return _with(flags);
    }

    /**
     * Mutant factory for constructing an instance with specified features
     * disabled.
     */
    public JSON without(Feature ... features)
    {
        int flags = _features;
        for (Feature feature : features) {
            flags &= ~feature.mask();
        }
        return _with(flags);
    }

    /**
     * Internal mutant factory method used for constructing
     */
    protected final JSON _with(int features)
    {
        if (_features == features) {
            return this;
        }
        return _with(features, _jsonFactory, _treeCodec,
                _reader.withFeatures(features), _writer.withFeatures(features),
                _prettyPrinter);
    }
    
    /*
    /**********************************************************************
    /* Methods sub-classes must override
    /**********************************************************************
     */
    
    protected final JSON _with(int features,
            JsonFactory jsonF, TreeCodec trees,
            JSONReader reader, JSONWriter writer,
            PrettyPrinter pp)
    {
        if (getClass() != JSON.class) {
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSON(features, jsonF, trees, reader, writer, pp);
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
        _writerForOperation(jgen).writeValue(value);
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
            result = _readerForOperation(jp).readList();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _readerForOperation(jp).readList();
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

    public Object[] arrayFromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        Object[] result;
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            jp = _initForReading((JsonParser) source);
            result = _readerForOperation(jp).readArray();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _readerForOperation(jp).readArray();
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

    @SuppressWarnings("unchecked")
    public <T> Map<T,Object> mapFromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        Map<Object,Object> result;
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            jp = _initForReading((JsonParser) source);
            result = _readerForOperation(jp).readMap();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _readerForOperation(jp).readMap();
                closed = true;
            } finally {
                if (!closed) {
                    _close(jp);
                }
            }
        }
        // Need to consume the token too
        jp.clearCurrentToken();
        return (Map<T,Object>) result;
    }

    /**
     * Read method that will take given JSON Source (of one of supported types),
     * read contents and map it to one of simple mappings ({@link java.util.Map}
     * for JSON Objects, {@link java.util.List} for JSON Arrays, {@link java.lang.String}
     * for JSON Strings, null for JSON null, {@link java.lang.Boolean} for JSON booleans
     * and {@link java.lang.Number} for JSON numbers.
     *<p>
     * Supported source types include:
     *<ul>
     * <li>{@link java.io.InputStream}</li>
     * <li>{@link java.io.Reader}</li>
     * <li>{@link java.io.File}</li>
     * <li>{@link java.net.URL}</li>
     * <li>{@link java.lang.String}</li>
     * <li><code>byte[]</code></li>
     * <li><code>char[]</code></li>
     *</ul>
     */
    public Object fromJSON(Object source) throws IOException, JSONObjectException
    {
        JsonParser jp;
        Object result;
        if (source instanceof JsonParser) {
            jp = _initForReading((JsonParser) source);
            result = _readerForOperation(jp).readValue();
        } else {
            jp = _parser(source);
            boolean closed = false;
            try {
                _initForReading(_config(jp));
                result = _readerForOperation(jp).readValue();
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
            _writerForOperation(jgen).writeValue(value);
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

    protected JSONWriter _writerForOperation(JsonGenerator jgen) {
        return _writer.perOperationInstance(jgen);
    }

    /*
    /**********************************************************************
    /* Internal methods, reading
    /**********************************************************************
     */
    
    protected JSONReader _readerForOperation(JsonParser jp) {
        return _reader.perOperationInstance(jp);
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
        } else if (isEnabled(Feature.PRETTY_PRINT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        return jgen;
    }

    protected JsonParser _config(JsonParser jp)
    {
        // nothing to do, yet
        return jp;
    }

    protected void _close(Closeable cl) {
        try {
            cl.close();
        } catch (IOException ioe) { }
    }
}
