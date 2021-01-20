package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

public interface ValueWriter
{
    public void writeValue(JSONWriter context, JsonGenerator g, Object value)
        throws JacksonException;

    /*
    /**********************************************************************
    /* Minimal metadata
    /**********************************************************************
     */

    /**
     * Accessor for non-generic (type-erased) type of values this reader
     * produces from input.
     */
    public Class<?> valueType();
}
