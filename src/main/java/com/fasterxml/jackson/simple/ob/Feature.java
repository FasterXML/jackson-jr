package com.fasterxml.jackson.simple.ob;

/**
 * Simple on/off features for {@link JSON}.
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
