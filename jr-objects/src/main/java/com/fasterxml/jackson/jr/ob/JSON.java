package com.fasterxml.jackson.jr.ob;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.tree.ArrayTreeNode;
import com.fasterxml.jackson.core.tree.ObjectTreeNode;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
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
@SuppressWarnings("resource")
public class JSON
    implements
        ObjectReadContext, ObjectWriteContext, // since 3.0
        Versioned
{
    /**
     * Simple on/off (enabled/disabled) features for {@link JSON}; used for simple configuration
     * aspects.
     */
    public enum Feature
    {
       /*
       /**********************************************************************
       /* Read-related features that do not affect caching
       /**********************************************************************
        */

       /**
        * When reading JSON Numbers, should {@link java.math.BigDecimal} be used
        * for floating-point numbers; or should {@link java.lang.Double} be used.
        * Trade-off is between accuracy -- only {@link java.math.BigDecimal} is
        * guaranteed to store the EXACT decimal value parsed -- and performance
        * ({@link java.lang.Double} is typically faster to parse).
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
       /* Write-related features that do not affect caching
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
        * Feature that determines whether Enum values are written using
        * numeric index (true), or String representation from calling
        * {@link Enum#toString()} (false).
        *<p>
        * Feature is disabled by default,
        * so that Enums are serialized as JSON Strings.
        */
       WRITE_ENUMS_USING_INDEX(false),

       /**
        * Feature that determines whether Date (and date/time) values
        * (and Date-based things like {@link java.util.Calendar}s) are to be
        * serialized as numeric timestamps (true),
        * or using a textual representation (false)
        *<p>
        * Feature is disabled by default, so that date/time values are
        * serialized as text, NOT timestamp.
        */
       WRITE_DATES_AS_TIMESTAMP(false),
       
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
        * called after <code>write()</code> method <b>that takes JsonGenerator
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
        * unrecognized type for which we do not have standard handler: if enabled,
        * will throw a {@link JSONObjectException}, if disabled simply
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
       /* Features that affect introspection
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
       HANDLE_JAVA_BEANS(true, true),

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
       WRITE_READONLY_BEAN_PROPERTIES(true, true),
       
       /**
        * Feature that determines whether access to {@link java.lang.reflect.Method}s and
        * {@link java.lang.reflect.Constructor}s that are used with dynamically
        * introspected Beans may be forced using
        * {@link java.lang.reflect.AccessibleObject#setAccessible} or not.
        *<p>
        * Feature is enabled by default, so that access may be forced.
        */
       FORCE_REFLECTION_ACCESS(true, true),

       /**
        * Whether "is-getters" (like <code>public boolean isValuable()</code>) are detected
        * for use or not. Note that in addition to naming, and lack of arguments, return
        * value also has to be <code>boolean</code> or <code>java.lang.Boolean</code>.
        */
       USE_IS_GETTERS(true, true),
       
       /**
        * Feature that enables use of public fields instead of setters and getters,
        * in cases where no setter/getter is available.
        *<p>
        * Feature is disabled by default (for backwards compatibility), so fields
        * are not used unless explicitly enabled.
        */
       USE_FIELDS(false, true),
       
       ;

       /*
       /**********************************************************************
       /* Enum impl
       /**********************************************************************
        */

       private final boolean _defaultState;

       /**
        * Flag for features that affect caching of readers, writers,
        * and changing of which needs to result in flushing.
        */
       private final boolean _affectsCaching;

       private final int _mask;

       private Feature(boolean defaultState) {
           this(defaultState, false);
       }
       
       private Feature(boolean defaultState, boolean affectsCaching) {
           _defaultState = defaultState;
           _affectsCaching = affectsCaching;
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

       /**
        * Method for calculating bitset of features that force flushing of
        * POJO handler caches.
        */
       public static int cacheBreakers()
       {
           int flags = 0;
           for (Feature value : values()) {
               if (value.affectsCaching()) {
                   flags |= value.mask();
               }
           }
           return flags;
       }
       
       public final boolean enabledByDefault() { return _defaultState; }
       public final boolean affectsCaching() { return _affectsCaching; }

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
    protected final TokenStreamFactory _streamFactory;

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
        this(new JsonFactory(), null, DEFAULT_FEATURES);
    }

    /**
     * @since 3.0
     */
    public JSON(TokenStreamFactory streamFactory) {
        this(streamFactory, null, DEFAULT_FEATURES);
    }

    protected JSON(TokenStreamFactory jsonF, TreeCodec trees, int features)
    {
        this(jsonF, trees, features,
                null, null, // reader, writer
                null);
    }

    protected JSON(TokenStreamFactory streamF, TreeCodec trees, int features,
            JSONReader r, JSONWriter w,
            PrettyPrinter pp)
    {
        _features = features;
        _streamFactory = streamF;
        _treeCodec = trees;
        TypeDetector td = _defaultTypeDetector(streamF, features);
        _reader = (r == null) ? _defaultReader(features, trees, td) : r;
        _writer = (w == null) ? _defaultWriter(features, trees, td) : w;
        _prettyPrinter = pp;
    }

    protected TypeDetector _defaultTypeDetector(TokenStreamFactory streamF, int features) {
        return TypeDetector.blueprint(streamF, features);
    }

    protected JSONReader _defaultReader(int features, TreeCodec tc, TypeDetector td) {
        return new JSONReader(features, td, tc,
                CollectionBuilder.defaultImpl(), MapBuilder.defaultImpl());
    }

    protected JSONWriter _defaultWriter(int features, TreeCodec tc, TypeDetector td) {
        return new JSONWriter(features, td, tc);
    }

    /*
    /**********************************************************************
    /* Adapting
    /**********************************************************************
     */

    // // // !!! TODO, maybe?

    /*
    public ObjectReadContext asObjectReadContext() {
//        return new JSONAsObjectCodec(this);
    }

    public ObjectWriteContext asObjectWriteContext() {
//      return new JSONAsObjectCodec(this);
  }
  */

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

    public JSON with(TokenStreamFactory f)
    {
        if (f == _streamFactory) {
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
        return _with(_features, _streamFactory, c,
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
        return _with(_features, _streamFactory, _treeCodec,
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
        return _with( _features, _streamFactory, _treeCodec,
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
        return _with(_features, _streamFactory, _treeCodec,
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
        return _with(_features, _streamFactory, _treeCodec,
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
        return _with(_features, _streamFactory, _treeCodec,
                r, _writer, _prettyPrinter);
    }
    
    /**
     * Mutant factory for constructing an instance with specified feature
     * enabled or disabled (depending on <code>state</code>), and returning
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
        return _with(features, _streamFactory, _treeCodec,
                _reader, _writer, _prettyPrinter);
    }

    /*
    /**********************************************************************
    /* Methods sub-classes must override
    /**********************************************************************
     */
    
    protected final JSON _with(int features,
            TokenStreamFactory jsonF, TreeCodec trees,
            JSONReader reader, JSONWriter writer,
            PrettyPrinter pp)
    {
        if (getClass() != JSON.class) {
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSON(jsonF, trees, features, reader, writer, pp);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    public TreeCodec getTreeCodec() {
        return _treeCodec;
    }

    public TokenStreamFactory getStreamingFactory() {
        return _streamFactory;
    }

    public final boolean isEnabled(Feature f) {
        return (f.mask() & _features) != 0;
    }
    
    /*
    /**********************************************************************
    /* API: writing Simple objects as JSON
    /**********************************************************************
     */

    public String asString(Object value) throws IOException, JSONObjectException
    {
        SegmentedStringWriter sw = new SegmentedStringWriter(_streamFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _streamFactory.createGenerator(this, sw));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    public byte[] asBytes(Object value) throws IOException, JSONObjectException
    {
        ByteArrayBuilder bb = new ByteArrayBuilder(_streamFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _streamFactory.createGenerator(this, bb, JsonEncoding.UTF8));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
        byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }

    public void write(Object value, JsonGenerator gen) throws IOException, JSONObjectException {
        // NOTE: no call to _config(); assumed to be fully configured
        _writerForOperation(gen).writeValue(value);
        if (Feature.FLUSH_AFTER_WRITE_VALUE.isEnabled(_features)) {
            gen.flush();
        }
    }

    public void write(Object value, OutputStream out) throws IOException, JSONObjectException {
        _writeAndClose(value, _streamFactory.createGenerator(this, out));
    }

    public void write(Object value, Writer w) throws IOException, JSONObjectException {
        _writeAndClose(value, _streamFactory.createGenerator(this, w));
    }

    public void write(Object value, File f) throws IOException, JSONObjectException {
        _writeAndClose(value, _streamFactory.createGenerator(this, f, JsonEncoding.UTF8));
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
                _config(_streamFactory.createGenerator(this, out)), true);
    }

    public JSONComposer<OutputStream> composeTo(Writer w) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _config(_streamFactory.createGenerator(this, w)), true);
    }

    public JSONComposer<OutputStream> composeTo(File f) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _config(_streamFactory.createGenerator(this, f, JsonEncoding.UTF8)), true);
    }

    public JSONComposer<String> composeString() throws IOException, JSONObjectException {
        SegmentedStringWriter out = new SegmentedStringWriter(_streamFactory._getBufferRecycler());
        JsonGenerator gen = _config(_streamFactory.createGenerator(this, out));
        return JSONComposer.stringComposer(_features, gen, out);
    }

    public JSONComposer<byte[]> composeBytes() throws IOException, JSONObjectException {
        ByteArrayBuilder out = new ByteArrayBuilder(_streamFactory._getBufferRecycler());
        JsonGenerator gen = _config(_streamFactory.createGenerator(this, out));
        return JSONComposer.bytesComposer(_features, gen, out);
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

    public List<Object> listFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            JsonParser p = _initForReading((JsonParser) source);
            List<Object> result = _readerForOperation(p).readList();
            // Need to consume the token too
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            List<Object> result = _readerForOperation(p).readList();
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    public <T> List<T> listOfFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            // note: no call to _config(), should come pre-configured
            JsonParser p = _initForReading((JsonParser) source);
            List<T> result = _readerForOperation(p).readListOf(type);
            // Need to consume the token too
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            List<T> result = _readerForOperation(p).readListOf(type);
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    public Object[] arrayFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            Object[] result = _readerForOperation(p).readArray();
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            Object[] result = _readerForOperation(p).readArray();
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    public <T> T[] arrayOfFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            T[] result = _readerForOperation(p).readArrayOf(type);
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            T[] result = _readerForOperation(p).readArrayOf(type);
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Map<T,Object> mapFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            Map<Object,Object> result = _readerForOperation(p).readMap();
            p.clearCurrentToken();
            return (Map<T,Object>) result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            Map<Object,Object> result = _readerForOperation(p).readMap();
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return (Map<T,Object>) result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    public <T> T beanFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            T result = _readerForOperation(p).readBean(type);
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            T result = _readerForOperation(p).readBean(type);
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
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
    public Object anyFrom(Object source) throws IOException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            Object result = _readerForOperation(p).readValue();
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            Object result = _readerForOperation(p).readValue();
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            _close(p, e);
            return null;
        }
    }

    /**
     * Method for reading content as a JSON Tree (of type that configured
     * {@link TreeCodec}, see {@link #with(TreeCodec)}) supports.
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeNode> TreeNode treeFrom(Object source)
            throws IOException, JSONObjectException
    {
        if (_treeCodec == null) {
             _noTreeCodec("read TreeNode");
        }
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            T result = (T) _treeCodec.readTree(p);
            p.clearCurrentToken();
            return result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            T result = (T) _treeCodec.readTree(p);
            JsonParser p0 = p;
            p = null;
            _close(p0, null);
            return result;
        } catch (Exception e) {
            _close(p, e);
            return null;
        }
    }
    /*
    /**********************************************************************
    /* ObjectReadContext: Config access (bogus)
    /**********************************************************************
     */

    @Override
    public int getFormatReadFeatures(int defaults) {
        return defaults;
    }

    @Override
    public TokenStreamFactory getParserFactory() {
        return _streamFactory;
    }

    /*
    /**********************************************************************
    /* ObjectReadContext: databind
    /**********************************************************************
     */
    
    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        if (_treeCodec == null) {
            _noTreeCodec("write TreeNode");
        }
        return _treeCodec.readTree(p);
    }

    @Override
    public <T> T readValue(JsonParser p, Class<T> valueType)
            throws IOException
    {
        // 11-Oct-2017, tatu: Not sure this is sufficient but it's best we got:
        // !!! TODO: maybe support array types?
        T result = _readerForOperation(p).readBean(valueType);
        p.clearCurrentToken();
        return result;
    }

    @Override
    public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef)
        throws IOException
    {
        // 11-Oct-2017, tatu: Not sure what to do here really
        throw new UnsupportedOperationException("TypeReference<?> not support by jackson-jr");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readValue(JsonParser p, ResolvedType type) throws IOException {
        return (T) readValue(p, type.getRawClass());
    }

    /*
    /**********************************************************************
    /* ObjectWriteContext: Config access (bogus)
    /**********************************************************************
     */
    
    @Override
    public FormatSchema getSchema() {
        return null;
    }

    @Override
    public CharacterEscapes getCharacterEscapes() {
        return null;
    }

    @Override
    public PrettyPrinter getPrettyPrinter() {
        return null;
    }

    @Override
    public SerializableString getRootValueSeparator(SerializableString defaultSeparator) {
        return defaultSeparator;
    }

    @Override
    public int getGeneratorFeatures(int defaults) {
        return defaults;
    }

    @Override
    public int getFormatWriteFeatures(int defaults) {
        return defaults;
    }

    @Override
    public TokenStreamFactory getGeneratorFactory() {
        return _streamFactory;
    }

    /*
    /**********************************************************************
    /* ObjectWriteContext: Serialization
    /**********************************************************************
     */
    
    @Override
    public void writeValue(JsonGenerator g, Object value) throws IOException {
        write(value, g);
    }

    /*
    /**********************************************************************
    /* ObjectWriteContext: Tree support
    /**********************************************************************
     */

    @Override
    public void writeTree(JsonGenerator g, TreeNode tree) throws IOException {
        if (_treeCodec == null) {
            _noTreeCodec("write TreeNode");
        }
        _treeCodec.writeTree(g, tree);
    }

    /**
     * Convenience method, equivalent to:
     *<pre>
     *   getTreeCodec().createArrayNode();
     *</pre>
     * Note that for call to succeed a {@link TreeCodec} must have been
     * configured with this instance using {@link #with(TreeCodec)} method.
     */
    @Override
    public ArrayTreeNode createArrayNode() {
         if (_treeCodec == null) {
              _noTreeCodec("create Object node");
          }
         return _treeCodec.createArrayNode();
    }
    
    /**
     * Convenience method, equivalent to:
     *<pre>
     *   getTreeCodec().createObjectNode();
     *</pre>
     * Note that for call to succeed a {@link TreeCodec} must have been
     * configured with this instance using {@link #with(TreeCodec)} method.
     */
    @Override
    public ObjectTreeNode createObjectNode() {
         if (_treeCodec == null) {
              _noTreeCodec("create Object node");
          }
         return _treeCodec.createObjectNode();
    }

    @Override
    public int getParserFeatures(int defaults) {
        return defaults;
    }

    /*
    /**********************************************************************
    /* Internal methods, writing
    /**********************************************************************
     */

    protected final void _writeAndClose(Object value, JsonGenerator g)
        throws IOException
    {
        boolean closed = false;
        try {
            _config(g);
            _writerForOperation(g).writeValue(value);
            closed = true;
            g.close();
        } finally {
            if (!closed) {
                // need to catch possible failure, so as not to mask problem
                try {
                    g.close();
                } catch (IOException ioe) { }
            }
        }
    }

    protected JSONWriter _writerForOperation(JsonGenerator gen) {
        return _writer.perOperationInstance(_features, gen);
    }

    /*
    /**********************************************************************
    /* Internal methods, reading
    /**********************************************************************
     */
    
    protected JSONReader _readerForOperation(JsonParser p) {
        return _reader.perOperationInstance(_features, p);
    }

    protected JsonParser _parser(Object source) throws IOException, JSONObjectException
    {
        final TokenStreamFactory f = _streamFactory;
        final Class<?> type = source.getClass();
        if (type == String.class) {
            return f.createParser(this, (String) source);
        }
        if (type == byte[].class) {
            return f.createParser(this, (byte[]) source);
        }
        if (source instanceof InputStream) {
            return f.createParser(this, (InputStream) source);
        }
        if (source instanceof Reader) {
            return f.createParser(this, (Reader) source);
        }
        if (source instanceof URL) {
            return f.createParser(this, (URL) source);
        }
        if (type == char[].class) {
            return f.createParser(this, new CharArrayReader((char[]) source));
        }
        if (source instanceof File) {
            return f.createParser(this, (File) source);
        }
        if (source instanceof CharSequence) {
            return f.createParser(this, ((CharSequence) source).toString());
        }
        throw new JSONObjectException("Can not use Source of type "+source.getClass().getName()
                +" as input (use an InputStream, Reader, String, byte[], File or URL");
    }

    protected JsonParser _initForReading(JsonParser p) throws IOException
    {
        /* First: must point to a token; if not pointing to one, advance.
         * This occurs before first read from JsonParser, as well as
         * after clearing of current token.
         */
        JsonToken t = p.currentToken();
        if (t == null) { // and then we must get something...
            t = p.nextToken();
            if (t == null) { // not cool is it?
                throw JSONObjectException.from(p, "No content to map due to end-of-input");
            }
        }
        return p;
    }
    
    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    protected JsonGenerator _config(JsonGenerator g)
    {
        // First, possible pretty printing
        PrettyPrinter pp = _prettyPrinter;
        if (pp != null) {
            if (pp instanceof Instantiatable<?>) {
                pp = (PrettyPrinter) ((Instantiatable<?>) pp).createInstance();
            }
            g.setPrettyPrinter(pp);
        } else if (isEnabled(Feature.PRETTY_PRINT_OUTPUT)) {
            g.useDefaultPrettyPrinter();
        }
        return g;
    }

    protected JsonParser _config(JsonParser p)
    {
        // nothing to do, yet
        return p;
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

    protected <T> T _closeWithError(Closeable cl, Exception e) throws IOException {
        if (cl != null) {
            try {
                cl.close();
            } catch (Exception secondaryEx) {
                e.addSuppressed(secondaryEx);
            }
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new IOException(e); // should never occur
    }

    protected void _noTreeCodec(String msg) {
         throw new IllegalStateException("JSON instance does not have configured TreeCodec to "+msg);
    }
}
