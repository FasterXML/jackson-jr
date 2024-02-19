package tools.jackson.jr.ob.impl;

import java.util.*;

import tools.jackson.core.*;
import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.CollectionBuilder;
import tools.jackson.jr.ob.api.ValueReader;

/**
 * Reader for typed {@link java.util.Collection} values.
 */
public class CollectionReader extends ValueReader
{
    protected final Class<?> _collectionType;
    protected final ValueReader _valueReader;

    public CollectionReader(Class<?> t, ValueReader vr) {
        super(t);
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
    public Object readNext(JSONReader r, JsonParser p) throws JacksonException {
        if (p.nextToken() != JsonToken.START_ARRAY) {
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            throw JSONObjectException.from(p, "Unexpected token %s; should get START_ARRAY",
                    p.currentToken());
        }
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
        
    @Override
    public Object read(JSONReader r, JsonParser p) throws JacksonException {
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