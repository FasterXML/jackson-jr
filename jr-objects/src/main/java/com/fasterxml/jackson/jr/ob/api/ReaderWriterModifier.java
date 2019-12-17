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

    public ValueReader modifyValueReader(JSONReader readContext,
            Class<?> type, ValueReader defaultReader) {
        return null;
    }

    // // Writer handling

    public ValueWriter modifyValueWriter(JSONWriter writeContext,
            Class<?> type, ValueWriter defaultWriter) {
        return null;
    }
}
