package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.ob.impl.BeanPropertyWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * API to implement to provide custom {@link ValueReader}s and
 * writers ({@code BeanPropertyWriter[]}).
 *
 * @since 2.10
 */
public abstract class ReaderWriterProvider {

    // // Reader access

    public ValueReader findPOJOReader(JSONReader context, Class<?> type) {
        return null;
    }

    public ValueReader findEnumReader(JSONReader context, Class<?> type) {
        return null;
    }

    public ValueReader findCollectionReader(JSONReader context, Class<?> type,
            ValueReader readerForValues) {
        return null;
    }

    public ValueReader findMapReader(JSONReader context, Class<?> type,
            ValueReader readerForValues) {
        return null;
    }

    // // Writer access

    public BeanPropertyWriter[] findWriter(JSONWriter context, Class<?> type) {
        return null;
    }
}
