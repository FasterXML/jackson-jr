package tools.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.type.ResolvedType;

import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;

/**
 * API to implement to provide custom {@link ValueReader}s and
 * {@link ValueWriter}s.
 */
public abstract class ReaderWriterProvider
{
    // // Reader access

    /**
     * Method called to find custom reader for given type that is NOT one of
     * special container types ({@link java.util.Collection},
     * {@link java.util.Map}): typically value is a scalar, Bean or Enum.
     *
     * @param readContext context object that may be needed for resolving dependant
     *    readers
     * @param type Raw type of bean to find reader for
     */
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        return null;
    }

    public ValueReader findCollectionReader(JSONReader readContext, Class<?> type,
            ResolvedType valueType, ValueReader readerForValues) {
        return null;
    }

    public ValueReader findMapReader(JSONReader readContext, Class<?> type,
            ResolvedType valueType, ValueReader readerForValues) {
        return null;
    }

    // // Writer access

    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        return null;
    }

    /**
     * Implementation that allows chaining of two providers, one (first) with higher precedence
     * than the other (second).
     *
     * @since 2.11
     */
    public static class Pair extends ReaderWriterProvider
    {
        protected final ReaderWriterProvider _primary, _secondary;

        protected Pair(ReaderWriterProvider p, ReaderWriterProvider s) {
            _primary = p;
            _secondary = s;
        }

        /**
         * Factory method for "combining" given 2 providers so that if neither is {@code null},
         * a {@link Pair} is constructed; otherwise if one is non-null, that provider is returned;
         * or if both are {@code null}s, {@code null} is returned.
         *
         * @param primary Primary provider
         * @param secondary Secondary provider
         *
         * @return Provider instance either constructed (2 non-null providers), or non-null provider
         *    given as-is, or, if both nulls, {@code null}.
         */
        public static ReaderWriterProvider of(ReaderWriterProvider primary, ReaderWriterProvider secondary) {
            if (primary == null) {
                return secondary;
            }
            if (secondary == null) {
                return primary;
            }
            return new Pair(primary, secondary);
        }

        @Override
        public ValueReader findValueReader(JSONReader ctxt, Class<?> type) {
            ValueReader r = _primary.findValueReader(ctxt, type);
            return (r == null) ? _secondary.findValueReader(ctxt, type) : r;
        }

        @Override
        public ValueReader findCollectionReader(JSONReader ctxt, Class<?> type,
                ResolvedType valueType, ValueReader readerForValues) {
            ValueReader r = _primary.findCollectionReader(ctxt, type, valueType, readerForValues);
            return (r == null) ? _secondary.findCollectionReader(ctxt, type, valueType, readerForValues) : r;
        }

        @Override
        public ValueReader findMapReader(JSONReader ctxt, Class<?> type,
                ResolvedType valueType, ValueReader readerForValues) {
            ValueReader r = _primary.findMapReader(ctxt, type, valueType, readerForValues);
            return (r == null) ? _secondary.findMapReader(ctxt, type, valueType, readerForValues) : null;
        }

        // // Writer access

        @Override
        public ValueWriter findValueWriter(JSONWriter ctxt, Class<?> type) {
            ValueWriter w = _primary.findValueWriter(ctxt, type);
            return (w == null) ? _secondary.findValueWriter(ctxt, type) : w;
        }
    }
}
