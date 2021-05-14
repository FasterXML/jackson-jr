package com.fasterxml.jackson.jr.ob;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.Instantiatable;
import com.fasterxml.jackson.jr.ob.api.*;
import com.fasterxml.jackson.jr.ob.comp.CollectionComposer;
import com.fasterxml.jackson.jr.ob.comp.ComposerBase;
import com.fasterxml.jackson.jr.ob.comp.MapComposer;
import com.fasterxml.jackson.jr.ob.impl.*;

/**
 * Main entry point for functionality for reading and writing JSON
 * and configuring details of reading and writing.
 *<p>
 * Note that instances are fully immutable, and thereby thread-safe.
 *<p>
 * Note on source types: source to read is declared as {@link java.lang.Object}
 * but covers following types:
 *<ul>
 * <li>{@link InputStream}</li>
 * <li>{@link Reader}</li>
 * <li>{@code byte[]}</li>
 * <li>{@code char[]}</li>
 * <li>{@link String}/{@link CharSequence}</li>
 * <li>{@link URL}</li>
 * <li>{@link File}</li>
 * </ul>
 *
 */
@SuppressWarnings("resource")
public class JSON implements Versioned
{
    /**
     * Simple on/off (enabled/disabled) features for {@link JSON}; used for simple
     * configuration aspects.
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
         * Note that it is up to {@link com.fasterxml.jackson.jr.ob.api.MapBuilder}
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
         *
         * @since 2.7
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
        /* Features that affect introspection and thereby affect caching
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
         *
         * @since 2.5
         */
        USE_IS_GETTERS(true, true),

        /**
         * Feature that enables use of public fields instead of setters and getters,
         * in cases where no setter/getter is available.
         *<p>
         * Feature is <b>enabled</b> by default since 2.10 (but was <b>disabled</b> for
         * 2.8 and 2.9), so public fields are discovered by default.
         *
         * @since 2.8 (enabled by default since 2.10)
         */
        USE_FIELDS(true, true),

        /**
         * Feature that enables serialization and deserialization of non-final static fields.
         *<p>
         * Feature did not exist, but was implicitly <b>enabled</b> by default. <b>disabled</b> for
         * 2.13).
         *
         * @since 2.13
         */
        INCLUDE_STATIC_FIELDS(false, true),

        /**
         * Feature that will allow for more forgiving deserialization of incoming JSON. If enabled,
         * the bean properties will be matched using their lower-case equivalents, meaning that any
         * case-combination (incoming and matching names are canonicalized by lower-casing) should work.
         * <p>
         * Note that there is additional performance overhead since incoming property names need to be lower-cased
         * before comparison, for cases where there are upper-case letters. Overhead for names that are already
         * lower-case should be negligible however.
         *<p>
         * Feature is <b>disabled</b> by default.
         *
         * @since 2.13
         */
        ACCEPT_CASE_INSENSITIVE_PROPERTIES(false, true),

        /**
         * Feature that determines if Enum deserialization should be case sensitive or not. If enabled,
         * Enum deserialization will ignore case, that is, case of incoming String value and enum id
         * (dependant on other settings, either `name()`, `toString()`, or explicit override) do not need to
         * match.
         *<p>
         * Feature is <b>disabled</b> by default.
         *
         * @since 2.13
         */
        ACCEPT_CASE_INSENSITIVE_ENUMS(false, true)
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
         *
         * @since 2.8
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
         *
         * @since 2.8
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
    final static int DEFAULT_FEATURES = Feature.defaults();

    public final static int CACHE_FLAGS = Feature.cacheBreakers();

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

    // @since 2.11
    protected final ValueReaderLocator _valueReaderLocator;

    // @since 2.11
    protected final ValueWriterLocator _valueWriterLocator;

    /**
     * Blueprint instance of the reader to use for reading JSON as simple
     * Objects.
     */
    protected final JSONReader _reader;

    /**
     * Blueprint instance of the writer to use for writing JSON given
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
    /* Builder (new in 2.11)
    /**********************************************************************
     */

    /**
     * Builder class that needs to be used for certain kind of "static" configuration
     * (settings that can not vary on per-call basis for {@link JSON}), such as
     * Extension registration.
     *
     * @since 2.11
     */
    public static class Builder {
        // Configuration, simple settings

        protected int _features = DEFAULT_FEATURES;
        protected PrettyPrinter _prettyPrinter;

        // Configuration, helper objects

        protected final JsonFactory _streamFactory;
        protected TreeCodec _treeCodec;

        protected JSONReader _reader;
        protected JSONWriter _writer;

        protected MapBuilder _mapBuilder;
        protected CollectionBuilder _collectionBuilder;

        // Configuration, extensions

        protected ExtContextImpl _extContext;

        public Builder(JsonFactory f) {
            _streamFactory = f;
        }

        public JSON build() {
            return new JSON(this);
        }

        // // // Mutators, features

        /**
         * Method for registering given extension to be used by {@link JSON}
         * this builder builds.
         *
         * @param extension Extension to register
         *
         * @return This builder for call chaining
         */
        public Builder register(JacksonJrExtension extension) {
            if (_extContext == null) {
                _extContext = new ExtContextImpl(this);
            }
            extension.register(_extContext);
            return this;
        }

        public Builder set(Feature feature, boolean state) {
            if (state) {
                _features |= feature.mask();
            } else {
                _features &= ~feature.mask();
            }
            return this;
        }

        /**
         * Method for enabling a set of Features for {@link JSON} to be built.
         *
         * @param features Features to enable
         *
         * @return This builder for call chaining
         */
        public Builder enable(Feature ... features)
        {
            for (Feature feature : features) {
                _features |= feature.mask();
            }
            return this;
        }

        /**
         * Method for disabling a set of Features for {@link JSON} to be built.
         *
         * @param features Features to disable
         *
         * @return This builder for call chaining
         */
        public Builder disable(Feature ... features)
        {
            for (Feature feature : features) {
                _features &= ~feature.mask();
            }
            return this;
        }

        // // // Mutators, helper objects

        /**
         * Method for specifying {@link PrettyPrinter} {@link JSON} to be built
         * should use on serialization.
         *
         * @param pp Pretty printer to use
         *
         * @return This builder for call chaining
         */
        public Builder prettyPrinter(PrettyPrinter pp) {
            _prettyPrinter = pp;
            return this;
        }

        /**
         * Method for specifying {@link TreeCodec} {@link JSON} to be built
         * should use for reading and writing {@link TreeNode} values.
         *<p>
         * Note: by default no {@link TreeCodec} is configured.
         *
         * @param tc TreeCodec to use
         *
         * @return This builder for call chaining
         */
        public Builder treeCodec(TreeCodec tc) {
            _treeCodec = tc;
            return this;
        }

        public Builder jsonReader(JSONReader r) {
            if (_collectionBuilder != null) {
                r = r.with(_collectionBuilder);
            }
            if (_mapBuilder != null) {
                r = r.with(_mapBuilder);
            }
            _reader = r;
            return this;
        }

        public Builder jsonWriter(JSONWriter w) {
            _writer = w;
            return this;
        }

        public Builder collectionBuilder(CollectionBuilder b) {
            _collectionBuilder = b;
            if (_reader != null) {
                _reader = _reader.with(b);
            }
            return this;
        }

        public Builder mapBuilder(MapBuilder b) {
            _mapBuilder = b;
            if (_reader != null) {
                _reader = _reader.with(b);
            }
            return this;
        }

        // // // Accessors

        public boolean isEnabled(Feature f) {
            return f.isEnabled(_features);
        }

        public int featureMask() { return _features; }
        public PrettyPrinter prettyPrinter() { return _prettyPrinter; }

        public JsonFactory streamFactory() { return _streamFactory; }
        public TreeCodec treeCodec() { return _treeCodec; }

        public ReaderWriterModifier readerWriterModifier() {
            return (_extContext == null) ? null : _extContext._rwModifier;
        }

        public ReaderWriterProvider readerWriterProvider() {
            return (_extContext == null) ? null : _extContext._rwProvider;
        }

        public JSONReader jsonReader() {
            // create default impl dynamically if necessary
            if (_reader == null) {
                _reader = new JSONReader(collectionBuilder(), mapBuilder());
            }
            return _reader;
        }

        public JSONWriter jsonWriter() {
            // create default impl dynamically if necessary
            if (_writer == null) {
                _writer = new JSONWriter();
            }
            return _writer;
        }

        public MapBuilder mapBuilder() {
            if (_mapBuilder == null) {
                _mapBuilder = MapBuilder.defaultImpl();
            }
            return _mapBuilder;
        }

        public CollectionBuilder collectionBuilder() {
            if (_collectionBuilder == null) {
                _collectionBuilder = CollectionBuilder.defaultImpl();
            }
            return _collectionBuilder;
        }
    }

    /*
    /**********************************************************************
    /* Basic construction
    /**********************************************************************
     */

    public JSON() {
        this(new JsonFactory());
    }

    public JSON(JsonFactory jsonF)
    {
        _features = DEFAULT_FEATURES;
        _jsonFactory = jsonF;
        _treeCodec = null;
        _valueReaderLocator = ValueReaderLocator.blueprint(null, null);
        _valueWriterLocator = ValueWriterLocator.blueprint(null, null);
        _reader = _defaultReader();
        _writer = _defaultWriter();
        _prettyPrinter = null;
    }

    /**
     * Constructor used when creating instance using {@link Builder}.
     *
     * @param b Builder that has configured settings to use.
     *
     * @since 2.11
     */
    public JSON(Builder b) {
        _features = b.featureMask();
        _jsonFactory = b.streamFactory();
        _treeCodec = b.treeCodec();

        final ReaderWriterProvider rwProvider = b.readerWriterProvider();
        final ReaderWriterModifier rwModifier = b.readerWriterModifier();
        ValueReaderLocator rloc = ValueReaderLocator.blueprint(null, null);
        ValueWriterLocator wloc = ValueWriterLocator.blueprint(null, null);
        if (rwProvider != null) {
            rloc = rloc.with(rwProvider);
            wloc = wloc.with(rwProvider);
        }
        if (rwModifier != null) {
            rloc = rloc.with(rwModifier);
            wloc = wloc.with(rwModifier);
        }
        _valueReaderLocator = rloc;
        _valueWriterLocator = wloc;

        _reader = b.jsonReader();
        _writer = b.jsonWriter();
        _prettyPrinter = b.prettyPrinter();
    }

    /**
     * @since 2.11
     */
    public static Builder builder() {
        return builder(new JsonFactory());
    }

    /**
     * @since 2.11
     */
    public static Builder builder(JsonFactory streamFactory) {
        return new Builder(streamFactory);
    }

    protected JSON(JSON base,
            int features,
            JsonFactory jsonF, TreeCodec trees,
            JSONReader r, JSONWriter w,
            PrettyPrinter pp)
    {
        _features = features;
        _jsonFactory = jsonF;
        _treeCodec = trees;
        _valueReaderLocator = base._valueReaderLocator;
        _valueWriterLocator = base._valueWriterLocator;
        _reader = r;
        _writer = w;
        _prettyPrinter = pp;
    }

    protected JSON(JSON base, ValueReaderLocator rloc, ValueWriterLocator wloc) {
        _features = base._features;
        _jsonFactory = base._jsonFactory;
        _treeCodec = base._treeCodec;
        _valueReaderLocator = rloc;
        _valueWriterLocator = wloc;
        _reader = base._reader;
        _writer = base._writer;
        _prettyPrinter = base._prettyPrinter;
    }

    protected JSONReader _defaultReader() {
        return new JSONReader(CollectionBuilder.defaultImpl(), MapBuilder.defaultImpl());
    }

    protected JSONWriter _defaultWriter() {
        return new JSONWriter();
    }

    /*
    /**********************************************************************
    /* Adapting
    /**********************************************************************
     */

    /**
     * Convenience method for constructing an adapter that uses this
     * instance as a {@link ObjectCodec}
     *
     * @return Wrapper over this object to adapt to {@link ObjectCodec} API
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
    /* Mutant factories, supported
    /**********************************************************************
     */

    /**
     * Mutant factory for constructing an instance with specified {@link PrettyPrinter},
     * and returning new instance (or, if there would be no change, this instance).
     *
     * @param pp {@link PrettyPrinter} to use for pretty-printing output (of {@code null} to disable
     *    pretty-printing)
     *
     * @return New instance with specified {@link PrettyPrinter} (if not same as currently configured);
     *   {@code this} otherwise.
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

        // 07-Jun-2019, tatu: May need to force clearing of state if cache-afflicted
        //    changes
        JSONReader r = _reader.withCacheCheck(features);
        JSONWriter w = _writer.withCacheCheck(features);

        return _with(features, _jsonFactory, _treeCodec,
                r, w, _prettyPrinter);
    }

    /*
    /**********************************************************************
    /* Mutant factories, deprecated
    /**********************************************************************
     */

    /**
     * Mutant factory method for constructing new instance with specified {@link JsonFactory}
     * if different from currently configured one (if not, return {@code this} as-is)
     *
     * @param f Jackson core format factory to use for low-level decoding/encoding
     *
     * @return New instance with specified factory (if not same as currently configured);
     *   {@code this} otherwise.
     *
     * @deprecated Since 2.11 should not try changing underlying stream factory but create
     *   a new instance if necessary: method will be removed from 3.0 at latest
     */
    @Deprecated
    public JSON with(JsonFactory f) {
        return _with(_features, f, _treeCodec, _reader, _writer, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link TreeCodec},
     * and returning new instance (or, if there would be no change, this instance).
     *
     * @param c Tree codec to use for reading/writing of tree representation
     *
     * @return New instance with specified codec (if not same as currently configured);
     *   {@code this} otherwise.
     *
     * @deprecated Since 2.11 should try using builder (see {@link #builder()} and create
     *    properly configured instance
     */
    @Deprecated
    public JSON with(TreeCodec c)
    {
        if (c == _treeCodec) {
            return this;
        }
        return _with(_features, _jsonFactory, c,
                _reader, _writer, _prettyPrinter);
    }

    /**
     * Mutant factory for constructing an instance with specified {@link JSONReader},
     * and returning new instance (or, if there would be no change, this instance).
     *
     * @param r Customized {@link JSONReader} to use instead of standard one
     *
     * @return New instance with specified {@link JSONReader} (if not same as currently configured);
     *   {@code this} otherwise.
     *
     * @deprecated Since 2.11 should try using builder (see {@link #builder()} and create
     *    properly configured instance
     */
    @Deprecated
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
     *
     * @param w Customized {@link JSONWriter} to use instead of standard one
     *
     * @return New instance with specified {@link JSONWriter} (if not same as currently configured);
     *   {@code this} otherwise.
     *
     * @deprecated Since 2.11 should try using builder (see {@link #builder()} and create
     *    properly configured instance
     */
    @Deprecated
    public JSON with(JSONWriter w)
    {
        if (w == _writer) {
            return this;
        }
        return _with( _features, _jsonFactory, _treeCodec,
                _reader, w, _prettyPrinter);
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
     * Mutant factory for constructing an instance with specified {@link ReaderWriterProvider},
     * and returning new instance (or, if there would be no change, this instance).
     *
     * @deprecated Since 2.11 should register using {@link JacksonJrExtension}
     */
    @Deprecated
    public JSON with(ReaderWriterProvider rwp) {
        ValueReaderLocator rloc = _valueReaderLocator.with(rwp);
        ValueWriterLocator wloc = _valueWriterLocator.with(rwp);
        if ((rloc == _valueReaderLocator) && (wloc == _valueWriterLocator))  {
            return this;
        }
        return new JSON(this, rloc, wloc);
    }

    /*
    /**********************************************************************
    /* Methods sub-classes must override
    /**********************************************************************
     */

    protected JSON _with(int features,
            JsonFactory jsonF, TreeCodec trees,
            JSONReader reader, JSONWriter writer,
            PrettyPrinter pp)
    {
        return new JSON(this, features, jsonF, trees, reader, writer, pp);
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
    /* Public factory methods for parsers, generators
    /**********************************************************************
     */

    /**
     * Factory method for opening a {@link JsonParser} to read content from one of
     * following supported sources
     *<ul>
     * <li>{@link InputStream}</li>
     * <li>{@link Reader}</li>
     * <li>{@code byte[]}</li>
     * <li>{@code char[]}</li>
     * <li>{@link String}/{@link CharSequence}</li>
     * <li>{@link URL}</li>
     * <li>{@link File}</li>
     * </ul>
     *<p>
     * Rules regarding closing of the underlying source follow rules
     * that {@link JsonFactory} has for its {@code createParser} method.
     *
     * @since 2.10
     */
    public JsonParser createParser(Object source) throws IOException, JSONObjectException {
        return _parser(source);
    }

    /*
    /**********************************************************************
    /* API: writing Simple objects as JSON
    /**********************************************************************
     */

    public String asString(Object value) throws IOException, JSONObjectException
    {
        SegmentedStringWriter sw = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _jsonFactory.createGenerator(sw));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JSONObjectException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    public byte[] asBytes(Object value) throws IOException, JSONObjectException
    {
        ByteArrayBuilder bb = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
        try {
            _writeAndClose(value, _jsonFactory.createGenerator(bb, JsonEncoding.UTF8));
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
                _config(_jsonFactory.createGenerator(out)), true);
    }

    public JSONComposer<OutputStream> composeTo(Writer w) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _config(_jsonFactory.createGenerator(w)), true);
    }

    public JSONComposer<OutputStream> composeTo(File f) throws IOException, JSONObjectException {
        return JSONComposer.streamComposer(_features,
                _config(_jsonFactory.createGenerator(f, JsonEncoding.UTF8)), true);
    }

    public JSONComposer<String> composeString() throws IOException, JSONObjectException {
        SegmentedStringWriter out = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        JsonGenerator gen = _config(_jsonFactory.createGenerator(out)
                .setCodec(asCodec()));
        return JSONComposer.stringComposer(_features, gen, out);
    }

    public JSONComposer<byte[]> composeBytes() throws IOException, JSONObjectException {
        ByteArrayBuilder out = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
        JsonGenerator gen = _config(_jsonFactory.createGenerator(out)
                .setCodec(asCodec()));
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
            _close(p0);
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
            _close(p0);
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
            _close(p0);
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
            _close(p0);
            return result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> mapFrom(Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            Map<?,?> result = _readerForOperation(p).readMap();
            p.clearCurrentToken();
            return (Map<String,Object>) result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            Map<?,?> result = _readerForOperation(p).readMap();
            JsonParser p0 = p;
            p = null;
            _close(p0);
            return (Map<String,Object>) result;
        } catch (Exception e) {
            return _closeWithError(p, e);
        }
    }

    /**
     * Read method for reading a {@link Map} of {@code type} (usually POJO) values.
     *
     * @since 2.10
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String,T> mapOfFrom(Class<T> type, Object source) throws IOException, JSONObjectException
    {
        if (source instanceof JsonParser) {
            JsonParser p = _initForReading((JsonParser) source);
            Map<?,?> result = _readerForOperation(p).readMapOf(type);
            p.clearCurrentToken();
            return (Map<String,T>) result;
        }
        JsonParser p = _parser(source);
        try {
            _initForReading(_config(p));
            Map<?,?> result = _readerForOperation(p).readMapOf(type);
            JsonParser p0 = p;
            p = null;
            _close(p0);
            return (Map<String,T>) result;
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
            _close(p0);
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
            _close(p0);
            return result;
        } catch (Exception e) {
            _closeWithError(p, e);
            return null;
        }
    }

    /**
     * Method for reading content as a JSON Tree (of type that configured
     * {@link TreeCodec}, see {@link #with(TreeCodec)}) supports.
     *
     * @since 2.8
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeNode> T treeFrom(Object source)
            throws IOException, JSONObjectException
    {
        if (_treeCodec == null) {
             _noTreeCodec("read `TreeNode`");
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
            _close(p0);
            return result;
        } catch (Exception e) {
            _closeWithError(p, e);
            return null;
        }
    }

    /*
    /**********************************************************************
    /* API: reading sequence of JSON values (LD-JSON and like)
    /**********************************************************************
     */

    /**
     * Method for creating {@link ValueIterator} for reading
     * <a href="https://en.wikipedia.org/wiki/JSON_streaming">streaming JSON</a>
     * content (specifically line-delimited and concatenated variants);
     * individual values are bound to specific Bean (POJO) type.
     *
     * @since 2.10
     */
    public <T> ValueIterator<T> beanSequenceFrom(Class<T> type, Object source)
        throws IOException, JSONObjectException
    {
        JsonParser p;
        final boolean managed = !(source instanceof JsonParser);

        if (managed) {
            p = _parser(source);
        } else {
            p = (JsonParser) source;
        }
        p = _initForReading(_config(p));
        JSONReader reader = _readerForOperation(p);
        return new ValueIterator<T>(ValueIterator.MODE_BEAN, type,
                p, reader, _treeCodec, managed);
    }

    /**
     * Method for creating {@link ValueIterator} for reading
     * <a href="https://en.wikipedia.org/wiki/JSON_streaming">streaming JSON</a>
     * content (specifically line-delimited and concatenated variants);
     * individual values are bound as "Simple" type: {@link java.util.Map},
     * {@link java.util.List}, {@link String}, {@link Number} or {@link Boolean}.
     *
     * @since 2.10
     */
    public ValueIterator<Object> anySequenceFrom(Object source)
        throws IOException, JSONObjectException
    {
        JsonParser p;
        final boolean managed = !(source instanceof JsonParser);

        if (managed) {
            p = _parser(source);
        } else {
            p = (JsonParser) source;
        }
        p = _initForReading(_config(p));
        JSONReader reader = _readerForOperation(p);
        return new ValueIterator<Object>(ValueIterator.MODE_ANY, Object.class,
                p, reader, _treeCodec, managed);
    }

    /**
     * Method for creating {@link ValueIterator} for reading
     * <a href="https://en.wikipedia.org/wiki/JSON_streaming">streaming JSON</a>
     * content (specifically line-delimited and concatenated variants);
     * individual values are bound as JSON Trees(of type that configured
     * {@link TreeCodec}, see {@link #with(TreeCodec)}) supports.
     */
    public <T extends TreeNode> ValueIterator<T> treeSequenceFrom(Object source)
        throws IOException, JSONObjectException
    {
        if (_treeCodec == null) {
            _noTreeCodec("read `TreeNode` sequence");
        }

        JsonParser p;
        final boolean managed = !(source instanceof JsonParser);

        if (managed) {
            p = _parser(source);
        } else {
            p = (JsonParser) source;
        }
        p = _initForReading(_config(p));
        JSONReader reader = _readerForOperation(p);
        return new ValueIterator<T>(ValueIterator.MODE_TREE, TreeNode.class,
                p, reader, _treeCodec, managed);
    }

    /*
    /**********************************************************************
    /* API: TreeNode construction
    /**********************************************************************
     */

    /**
     * Convenience method, equivalent to:
     *<pre>
     *   getTreeCodec().createArrayNode();
     *</pre>
     * Note that for call to succeed a {@link TreeCodec} must have been
     * configured with this instance using {@link #with(TreeCodec)} method.
     *
     * @since 2.8
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeNode> T createArrayNode() {
         if (_treeCodec == null) {
              _noTreeCodec("create Array node");
          }
         return (T) _treeCodec.createArrayNode();
    }

    /**
     * Convenience method, equivalent to:
     *<pre>
     *   getTreeCodec().createObjectNode();
     *</pre>
     * Note that for call to succeed a {@link TreeCodec} must have been
     * configured with this instance using {@link #with(TreeCodec)} method.
     *
     * @since 2.8
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeNode> T createObjectNode() {
         if (_treeCodec == null) {
              _noTreeCodec("create Object node");
          }
         return (T) _treeCodec.createObjectNode();
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
                _close(g);
            }
        }
    }

    protected JSONWriter _writerForOperation(JsonGenerator gen) {
        return _writer.perOperationInstance(_features,
                _valueWriterLocator, _treeCodec, gen);
    }

    /*
    /**********************************************************************
    /* Internal methods, reading
    /**********************************************************************
     */

    protected JSONReader _readerForOperation(JsonParser p) {
        return _reader.perOperationInstance(_features, _valueReaderLocator, _treeCodec, p);
    }

    protected JsonParser _parser(Object source) throws IOException, JSONObjectException
    {
        final JsonFactory f = _jsonFactory;
        final Class<?> type = source.getClass();
        if (type == String.class) {
            return f.createParser((String) source);
        }
        if (type == byte[].class) {
            return f.createParser((byte[]) source);
        }
        if (source instanceof InputStream) {
            return f.createParser((InputStream) source);
        }
        if (source instanceof Reader) {
            return f.createParser((Reader) source);
        }
        if (source instanceof URL) {
            return f.createParser((URL) source);
        }
        if (type == char[].class) {
            return f.createParser(new CharArrayReader((char[]) source));
        }
        if (source instanceof File) {
            return f.createParser((File) source);
        }
        if (source instanceof CharSequence) {
            return f.createParser(((CharSequence) source).toString());
        }
        throw new JSONObjectException("Can not use Source of type `"+source.getClass().getName()
+"` as input (use an `InputStream`, `Reader`, `String`/`CharSequence`, `byte[]`, `char[]`, `File` or `URL`");
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

    protected <T> T _closeWithError(Closeable cl, Exception e) throws IOException {
        _close(cl);
        return _throw(e);
    }

    protected void _close(Closeable cl) {
        if (cl != null) {
            try {
                cl.close();
            } catch (IOException ioe) { }
        }
    }

    protected <T> T _throw(Exception e) throws IOException {
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new IOException(e); // should never occur
    }

    protected void _noTreeCodec(String msg) {
         throw new IllegalStateException("JSON instance does not have configured `TreeCodec` to "+msg);
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    /**
     * Extension context implementation used when
     */
    private static class ExtContextImpl extends ExtensionContext {
        final Builder _builder;

        ReaderWriterProvider _rwProvider;
        ReaderWriterModifier _rwModifier;

        ExtContextImpl(Builder b) {
            _builder = b;
        }

        @Override
        public ExtensionContext setTreeCodec(TreeCodec tc) {
            _builder.treeCodec(tc);
            return this;
        }

        @Override
        public TreeCodec treeCodec() {
            return _builder.treeCodec();
        }

        @Override
        public ExtensionContext insertProvider(ReaderWriterProvider provider) {
            _rwProvider = ReaderWriterProvider.Pair.of(provider, _rwProvider);
            return this;
        }

        @Override
        public ExtensionContext appendProvider(ReaderWriterProvider provider) {
            _rwProvider = ReaderWriterProvider.Pair.of(_rwProvider, provider);
            return this;
        }

        @Override
        public ExtensionContext insertModifier(ReaderWriterModifier modifier) {
            _rwModifier = ReaderWriterModifier.Pair.of(modifier, _rwModifier);
            return this;
        }

        @Override
        public ExtensionContext appendModifier(ReaderWriterModifier modifier) {
            _rwModifier = ReaderWriterModifier.Pair.of(_rwModifier, modifier);
            return this;
        }
    }
}
