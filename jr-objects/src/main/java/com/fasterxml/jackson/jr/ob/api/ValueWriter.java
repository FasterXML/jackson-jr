package com.fasterxml.jackson.jr.ob.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 *
 * @since 2.10
 */
public interface ValueWriter {
    public void writeValue(JSONWriter context, JsonGenerator g, Object value)
        throws IOException;
}
