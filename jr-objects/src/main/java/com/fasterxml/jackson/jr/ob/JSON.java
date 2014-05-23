package com.fasterxml.jackson.jr.ob;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.Instantiatable;
import com.fasterxml.jackson.jr.ob.comp.CollectionComposer;
import com.fasterxml.jackson.jr.ob.comp.ComposerBase;
import com.fasterxml.jackson.jr.ob.comp.MapComposer;
import com.fasterxml.jackson.jr.ob.impl.*;

/**
 * Main entry point for functionality.
 *<p>
 * Note that instances are fully immutable, and thereby thread-safe.
 */
public class JSON implements Versioned
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
        * Note that it is up to {@link com.fasterxml.jackson.jr.ob.impl.MapBuilder}
        * to support this feature; custom implementations may ignore the setting.
        *<p>
        * Default setting is <code>true</code>, meaning that reader is expected to try to
        * preserve ordering of fields read.
        */
       PRESERVE_FIELD_ORDERING(true),

       /**
        * This feature determines whether {@link Map} instances constructed use
        * deferred materialization (as implemented by {@link DeferredMap}), in case
        * user has not specified custom {@link Map} implementation.
        * Enabling feature typically reduces initial value read time and moves
        * overhead to actual access of contents (materialization occurs when first
        * key or value access happens); this makes sense when only a subset of
        * data is accessed. Conversely, when traversing full object hierarchy, it
        * makes sense to disable this feature.
        *<p>
        * Default setting is <code>true</code>, meaning that reader is expected to try to
        */
       USE_DEFERRED_MAPS(true),

       /**
        * Whether "is-setters" (like <code>public boolean isValuable()</code>) are detected
        * for use or not. Note that in addition to naming, and lack of arguments, return
        * value also has to be <code>boolean</code> or <code>java.lang.Boolean</code>.
        */
       USE_IS_SETTERS(true),
       
       /**
        * When encountering duplicate keys for JSON Objects, should an exception
        * be thrown or not? If exception is not thrown, <b>the last</b> instance
        * from input document will be used.
        *<p>
        * Default setting is <code>true</code>, meaning that a
        * {@link JSONObjectException} will be thrown if duplicates are encountered.
        */
       FAIL_ON_DUPLICATE_MAP_KEYS(true),

       /**
        * When encountering a JSON Object property name for which there is no
        * matching Bean property, should an exception be thrown (true),
        * or should JSON Property value be quietly skipped (false)?
        *<p>
        * Default setting is <code>false</code>, meaning that unmappable
        * JSON Object properties will simply be ignored.
        */
       FAIL_ON_UNKNOWN_BEAN_PROPERTY(false),

       /*
       /**********************************************************************
       /* Write-related features
       /**********************************************************************
        */

       /**
        * Feature that defines what to do with {@link java.util.Map} entries and Java Bean
        * properties that have null as value: if enabled, they will be written out normally;
        * if disabled, such entries and properties will be ignored.
        *<p>
        * Default setting is <code>false</code> so that any null-valued properties
        * are ignored during serialization.
        */
       WRITE_NULL_PROPERTIES(false),

       /**
        * Feature that determines whether "read-only" properties of Beans
        * (properties that only have a getter but no matching setter) are
        * to be included in Bean serialization or not; if disabled,
        * only properties have have both setter and getter are serialized.
        * Note that feature is only used if {@link #HANDLE_JAVA_BEANS}
        * is also enabled.
        *<p>
        * Feature is enabled by default,
        * so that all Bean properties are serialized.
        */
       WRITE_READONLY_BEAN_PROPERTIES(true),

       /**
        * Feature that determines where Enum values are written using
        * numeric index (true), or String representation from calling
        * {@link Enum#toString()} (false).
        *<p>
        * Feature is disabled by default,
        * so that Enums are serialized as JSON Strings.
        */
       WRITE_ENUMS_USING_INDEX(false),

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

       /**
        * Feature that determines what happens when we encounter a value of
        * unrecognized type for which we do not have handling: if enabled,
        * will throw a {@link JsonObjectException}, if disabled simply
        * calls {@link Object#toString} and uses that JSON String as serialization.
        *<p>
        * NOTE: if {@link #HANDLE_JAVA_BEANS} is enabled, this setting typically
        * has no effect, since otherwise unknown types are recognized as
        * Bean types.
        *
        *<p>
        * Feature is disabled by default
        * so that no exceptions are thrown.
        */
       FAIL_ON_UNKNOWN_TYPE_WRITE(false),

       /*
       /**********************************************************************
       /* Other features
       /**********************************************************************
        */

       /**
        * Feature that determines whether Bean types (Java objects with
        * getters and setters that expose state to serialize) will be
        * recognized and handled or not.
        * When enabled, any types that are not recognized as standard JDK
        * data structures, primitives or wrapper values will be introspected
        * and handled as Java Beans (can be read/written as long as JSON
        * matches properties discovered); when disabled, they may only be serialized
        * (using {@link Object#toString} method), and can not be deserialized.
        *<p>
        * Feature is enabled by default, but can be disabled do avoid use
        * of Bean reflection for cases where it is not desired.
        */
       HANDLE_JAVA_BEANS(true),
       
       /**
        * Feature that determines whether access to {@link java.lang.reflect.Method}s and
        * {@link java.lang.reflect.Constructor}s that are used with dynamically
        * introspected Beans may be forced using
        * {@link java.lang.reflect.AccessibleObject#setAccessible} or not.
        *<p>
        * Feature is enabled by default, so that access may be forced.
        */
       FORCE_REFLECTION_ACCESS(true),
       
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
       
       public final boolean enabledByDefault() { return _defaultState; }

       public final int mask() { return _mask; }

       public final boolean isDisabled(int flags) {
           return (flags & _mask) == 0;
       }
       public final boolean isEnabled(int flags) {
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
        _reader = (r == null) ? _defaultReader(features, trees) : r;
        _writer = (w == null) ? _defaultWriter(features, trees) : w;
        _prettyPrinter = pp;
    }

    protected JSONReader _defaultReader(int features, TreeCodec tc) {
        return new JSONReader(features, TypeDetector.forReader(features), tc,
                CollectionBuilder.defaultImpl(), MapBuilder.defaultImpl());
    }

    protected JSONWriter _defaultWriter(int features, TreeCodec tc) {
        return new JSONWriter(features, TypeDetector.forWriter(features), tc);
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
     * Mutant factory for constructing an instance with specified {@link CollectionBuilder},
     * and returning new instance (or, if there would be no change, this instance).
     */
    public JSON with(CollectionBuilder b) {
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

    public JsonFactory getStreamingFactory() {
        return _jsonFactory;
    }

    public final boolean isEnabled(Feature f) {
        return (f.mask() & _features) != 0;
    }
    
    /*
    /**********************************************************************
    /* API: writing Simple objects as JSON
    /**********************************************************************
     */

    @SuppressWarnings("resource")
    public String asString(Object value) throws IOException, JSONObjectException
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
    public byte[] asBytes(Object value) throws IOException, JSONObjectException
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

    public void write(Object value, JsonGenerator jgen) throws IOException, JSONObjectException {
        // NOTE: no call to _config(); assumed to be fully configured
        _writerForOperation(jgen).writeValue(value);
        if (Feature.FLUSH_AFTER_WRITE_VALUE.isEnabled(_features)) {
            jgen.flush();
        }
    }

    public void write(Object value, OutputStream out) throws IOException, JSONObjectException {
        _writeAndClose(value, _jsonFactory.createGenerator(out));
    }

    public void write(Object value, Writer w) throws IOException, JSONObjectException {
        _writeAndClose(value, _jsonFactory.createGenerator(w));
    }

    public void write(Object value, File f) throws IOException, JSONObjectException {
        _writeAndClose(value, _jsonFactory.createGenerator(f, JsonEncoding.UTF8));
    }
    
    /*
    /**********************************************************************
    /* API: writing using Composers
    /**********************************************************************
     */

    public JSONComposer<OutputStream> composeUsing(JsonGenerator gen) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features, gen, false);
    }
    
    public JSONComposer<OutputStream> composeTo(OutputStream out) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _jsonFactory.createGenerator(out), true);
    }

    public JSONComposer<OutputStream> composeTo(Writer w) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _jsonFactory.createGenerator(w), true);
    }

    public JSONComposer<OutputStream> composeTo(File f) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _jsonFactory.createGenerator(f, JsonEncoding.UTF8), true);
    }
    
    @SuppressWarnings("resource")
    public JSONComposer<String> composeString() throws IOException, JSONObjectException {
        SegmentedStringWriter out = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        return JSONComposer.stringComposer(_features, _jsonFactory.createGenerator(out), out);
    }

    @SuppressWarnings("resource")
    public JSONComposer<byte[]> composeBytes() throws IOException, JSONObjectException {
        ByteArrayBuilder out = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
        return JSONComposer.bytesComposer(_features, _jsonFactory.createGenerator(out), out);
    }

    public CollectionComposer<?,List<Object>> composeList() {
        List<Object> list = new ArrayList<Object>();
        return composeCollection(list);
    }

    public <C extends Collection<Object>> CollectionComposer<?,C> composeCollection(C collection) {
        return new CollectionComposer<ComposerBase,C>(collection);
    }
    
    public MapComposer<?> composeMap() {
        return composeMap(new LinkedHashMap<String,Object>());
    }

    public MapComposer<?> composeMap(Map<String,Object> map) {
        return new MapComposer<ComposerBase>(map);
    }
    
    /*
    /**********************************************************************
    /* API: reading JSON as Simple Objects
    /**********************************************************************
     */

    @SuppressWarnings("resource")
    public List<Object> listFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            JsonParser jp = _initForReading((JsonParser) source);
            List<Object> result = _readerForOperation(jp).readList();
            // Need to consume the token too
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            List<Object> result = _readerForOperation(jp).readList();
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
    }

    @SuppressWarnings("resource")
    public <T> List<T> listOfFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            JsonParser jp = _initForReading((JsonParser) source);
            List<T> result = _readerForOperation(jp).readListOf(type);
            // Need to consume the token too
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            List<T> result = _readerForOperation(jp).readListOf(type);
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
    }
    
    @SuppressWarnings("resource")
    public Object[] arrayFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser jp = _initForReading((JsonParser) source);
            Object[] result = _readerForOperation(jp).readArray();
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            Object[] result = _readerForOperation(jp).readArray();
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
    }

    @SuppressWarnings("resource")
    public <T> T[] arrayOfFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser jp = _initForReading((JsonParser) source);
            T[] result = _readerForOperation(jp).readArrayOf(type);
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            T[] result = _readerForOperation(jp).readArrayOf(type);
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
    }
    
    @SuppressWarnings({ "unchecked", "resource" })
    public <T> Map<T,Object> mapFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser jp = _initForReading((JsonParser) source);
            Map<Object,Object> result = _readerForOperation(jp).readMap();
            jp.clearCurrentToken();
            return (Map<T,Object>) result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            Map<Object,Object> result = _readerForOperation(jp).readMap();
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return (Map<T,Object>) result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
    }

    @SuppressWarnings("resource")
    public <T> T beanFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser jp = _initForReading((JsonParser) source);
            T result = _readerForOperation(jp).readBean(type);
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            T result = _readerForOperation(jp).readBean(type);
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
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
    @SuppressWarnings("resource")
    public Object anyFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser jp = _initForReading((JsonParser) source);
            Object result = _readerForOperation(jp).readValue();
            jp.clearCurrentToken();
            return result;
        }
        JsonParser jp = _parser(source);
        try {
            _initForReading(_config(jp));
            Object result = _readerForOperation(jp).readValue();
            JsonParser jp0 = jp;
            jp = null;
            _close(jp0, null);
            return result;
        } catch (Exception e) {
            _close(jp, e);
            return null;
        }
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

    protected void _close(Closeable cl, Exception e) throws IOException {
        if (cl != null) {
            if (e == null) {
                cl.close();
            } else {
                try {
                    cl.close();
                } catch (Exception secondaryEx) {
                    // what should we do here, if anything?
                }
            }
        }
        if (e != null) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IOException(e); // should never occur
        }
    }
}
