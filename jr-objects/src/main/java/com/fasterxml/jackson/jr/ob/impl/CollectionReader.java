package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class CollectionReader extends ValueReader
{
    protected final Class<?> _collectionType;
    protected final ValueReader _valueReader;

    public CollectionReader(Class<?> t, ValueReader vr) {
        // some cleanup will be needed....
        if (t == Collection.class || t == List.class) { // since we default to ArrayList
            _collectionType = null;
        } else if (t == Set.class) {
            _collectionType = HashSet.class;
        } else if (t == SortedSet.class) {
            _collectionType = TreeSet.class;
        } else {
            _collectionType = t;
        }
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException {
        CollectionBuilder b = r._collectionBuilder(_collectionType);
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
        return b.buildCollection();
    }
}