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
    public Object read(JSONReader r, JsonParser p) throws IOException {
        CollectionBuilder b = r._collectionBuilder(null);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.emptyCollection();
        }
        Object value = _valueReader.read(r, p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.singletonCollection(value);
        }
        b = b.start().add(value);
        do {
            b = b.add(_valueReader.read(r, p));
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return b.buildArray(_elementType);
    }
}