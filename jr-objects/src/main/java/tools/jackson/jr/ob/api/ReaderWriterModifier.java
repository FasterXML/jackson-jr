package tools.jackson.jr.ob.api;

import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;
import tools.jackson.jr.ob.impl.POJODefinition;

/**
 * API to implement to apply modifications to {@link ValueReader}s and
 * {@link ValueWriter}s of all kinds (default scalar ones, custom ones,
 * POJO ones).
 */
public abstract class ReaderWriterModifier
{
    // // Basic POJO introspection

    public POJODefinition pojoDefinitionForDeserialization(JSONReader readContext,
            Class<?> pojoType)
    {
        // use default
        return null;
    }

    public POJODefinition pojoDefinitionForSerialization(JSONWriter writeContext,
            Class<?> pojoType) {
        // use default
        return null;
    }

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

    /**
     * Implementation that allows chaining of two modifiers, one (first) with higher precedence
     * than the other (second).
     */
    public static class Pair extends ReaderWriterModifier
    {
        protected final ReaderWriterModifier _primary, _secondary;

        protected Pair(ReaderWriterModifier p, ReaderWriterModifier s) {
            _primary = p;
            _secondary = s;
        }

        /**
         * Factory method for "combining" given 2 modifiers so that if neither is {@code null},
         * a {@link Pair} is constructed; otherwise if one is non-null, that provider is returned;
         * or if both are {@code null}s, {@code null} is returned.
         *
         * @param primary Primary provider
         * @param secondary Secondary provider
         *
         * @return Provider instance either constructed (2 non-null providers), or non-null provider
         *    given as-is, or, if both nulls, {@code null}.
         */
        public static ReaderWriterModifier of(ReaderWriterModifier primary, ReaderWriterModifier secondary) {
            if (primary == null) {
                return secondary;
            }
            if (secondary == null) {
                return primary;
            }
            return new Pair(primary, secondary);
        }
        
        @Override
        public POJODefinition pojoDefinitionForDeserialization(JSONReader ctxt,
                Class<?> pojoType) {
            POJODefinition def = _primary.pojoDefinitionForDeserialization(ctxt, pojoType);
            return (def == null) ? _secondary.pojoDefinitionForDeserialization(ctxt, pojoType) : def;
        }

        @Override
        public POJODefinition pojoDefinitionForSerialization(JSONWriter ctxt,
                Class<?> pojoType) {
            POJODefinition def = _primary.pojoDefinitionForSerialization(ctxt, pojoType);
            return (def == null) ? _secondary.pojoDefinitionForSerialization(ctxt, pojoType) : def;
        }
    
        @Override
        public ValueReader modifyValueReader(JSONReader ctxt,
                Class<?> type, ValueReader defaultReader) {
            defaultReader = _primary.modifyValueReader(ctxt, type, defaultReader);
            return _secondary.modifyValueReader(ctxt, type, defaultReader);
        }

        @Override
        public ValueWriter modifyValueWriter(JSONWriter ctxt,
                Class<?> type, ValueWriter defaultWriter) {
            defaultWriter = _primary.modifyValueWriter(ctxt, type, defaultWriter);
            return _secondary.modifyValueWriter(ctxt, type, defaultWriter);
        }

        @Override
        public ValueWriter overrideStandardValueWriter(JSONWriter ctxt,
                Class<?> type, int stdTypeId) {
            ValueWriter w = _primary.overrideStandardValueWriter(ctxt, type, stdTypeId);
            return (w == null) ? _secondary.overrideStandardValueWriter(ctxt, type, stdTypeId) : w;
        }
    }
}
