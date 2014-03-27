package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class CollectionReader extends ValueReader
{
    protected final Class<?> _collectionType;
    protected final ValueReader _valueReader;

    public CollectionReader(Class<?> t, ValueReader vr) {
        _collectionType = t;
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        // !!! TODO
        throw new UnsupportedOperationException();
    }
}