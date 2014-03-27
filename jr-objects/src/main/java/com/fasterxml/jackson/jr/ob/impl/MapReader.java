package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class MapReader extends ValueReader
{
    protected final Class<?> _mapType;
    protected final ValueReader _valueReader;

    public MapReader(Class<?> t, ValueReader vr) {
        _mapType = t;
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        // !!! TODO
        return null;
    }
}