package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * API to implement to apply modifications to {@link ValueReader}s and
 * {@link ValueWriter}s of all kinds (default scalar ones, custom ones,
 * POJO ones).
 *
 * @since 2.11
 */
public abstract class ReaderWriterModifier
{
    // // Reader handling

    /**
     * Method called after {@link ValueReader} to use has been constructed, but
     * before it is to be used for the first time. Method may either return that
     * reader as-is, or construct a different {@link ValueReader} and return that
     * to be used.
     *<p>
     * This method is often used to create a new {@link ValueReader} that needs to
     * delegate to the original reader for some uses, but not all.
     * 
     * @param readContext Context that may be used to access configuration
     * @param type Type of values to read
     * @param defaultReader {@link ValueReader} that is to be used
     *
     * @return either {@code defaultReader} as-is, or an alternate {@link ValueReader} to use.
     */
    public ValueReader modifyValueReader(JSONReader readContext,
            Class<?> type, ValueReader defaultReader) {
        return defaultReader;
    }

    // // Writer handling

    /**
     * Method called after {@link ValueWriter} to use has been constructed, but
     * before it is to be used for the first time. Method may either return that
     * writer as-is, or construct a different {@link ValueWriter} and return that
     * to be used.
     *<p>
     * Note that this method is NOT called for non-POJO JDK "standard" values that
     * jackson-jr supports (such as {@link java.lang.Number}s, {@link java.lang.String}
     * and {@link java.net.URL}); for these types, {@link #overrideStandardValueWriter}
     * is called instead.
     *<p>
     * This method is often used to create a new {@link ValueReader} that needs to
     * delegate to the original reader for some uses, but not all.
     * 
     * @param writeContext Context that may be used to access configuration
     * @param type Type of values to write
     * @param defaultWriter {@link ValueReader} that is to be used
     *
     * @return either {@code defaultReader} as-is, or an alternate {@link ValueWriter} to use;
     *   must not return {@code null}.
     */
    public ValueWriter modifyValueWriter(JSONWriter writeContext,
            Class<?> type, ValueWriter defaultWriter) {
        return defaultWriter;
    }

    /**
     * Method called instead of {@link #modifyValueWriter} for set of non-POJO
     * "standard" JDK types that do not have matching {@link ValueWriter} and are
     * normally directly serialized by {@link JSONWriter} itself.
     * Handler may either return {@code null} to indicate "no override" or return
     * custom {@link ValueWriter} to use.
     * 
     * @param writeContext Context that may be used to access configuration
     * @param type Type of values to write
     * @param stdTypeId Internal identifier of standard type (not usually useful,
     *    but could potentially be used for delegating)
     *
     * @return {@code null} if no override should occur, or {@link ValueWriter}
     *    to use.
     */
    public ValueWriter overrideStandardValueWriter(JSONWriter writeContext,
            Class<?> type, int stdTypeId) {
        return null;
    }
}
