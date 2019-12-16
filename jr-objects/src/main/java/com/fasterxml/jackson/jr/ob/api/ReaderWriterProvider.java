package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.type.ResolvedType;

/**
 * API to implement to provide custom {@link ValueReader}s and
 * {@link ValueWriter}s.
 *
 * @since 2.10
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
}
