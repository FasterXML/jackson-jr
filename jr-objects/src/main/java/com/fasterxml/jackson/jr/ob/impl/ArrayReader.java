package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class ArrayReader extends ValueReader
{
    protected final Class<?> _elementType;
    protected final ValueReader _valueReader;

    public ArrayReader(Class<?> t, ValueReader vr) {
        _elementType = t;
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        // !!! TODO
        throw new UnsupportedOperationException();
    }
}