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
     * for floating-point numbers or {@link java.lang.Double}?
     * is used.
     *<p>
     * Default setting is <code>false</code>, meaning that {@link java.lang.Double}
     * is used.
     */
    USE_BIG_DECIMAL_FOR_FLOATS(false),

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
    
    private Feature(boolean defaultState) {
        _defaultState = defaultState;
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

    public int mask() { return (1 << ordinal()); }
}
