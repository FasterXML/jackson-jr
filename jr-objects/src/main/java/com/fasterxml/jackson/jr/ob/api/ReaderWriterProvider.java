package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.ob.impl.BeanPropertyWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.type.ResolvedType;

/**
 * API to implement to provide custom {@link ValueReader}s and
 * writers ({@code BeanPropertyWriter[]}).
 *
 * @since 2.10
 */
public abstract class ReaderWriterProvider {

    // // Reader access

    /**
     * Method called to find custom reader for given type that is NOT one of
     * other "special" types ({java.lang.Enum}, {@link java.util.Collection},
     * {@link java.util.Map}).
     *
     * @param readContext context object that may be needed for resolving dependant
     *    readers
     * @param type Raw type of bean to find reader for
     */
    public ValueReader findBeanReader(JSONReader readContext, Class<?> type) {
        return null;
    }

    public ValueReader findEnumReader(JSONReader readContext, Class<?> type) {
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

    public BeanPropertyWriter[] findWriter(JSONWriter context, Class<?> type) {
        return null;
    }
}
