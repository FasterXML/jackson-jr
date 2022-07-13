package tools.jackson.jr.ob.impl;

import tools.jackson.core.*;
import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.CollectionBuilder;
import tools.jackson.jr.ob.api.ValueReader;

/**
 * Reader for typed Array values.
 */
public class ArrayReader extends ValueReader
{
    protected final Class<?> _elementType;
    protected final ValueReader _valueReader;

    public ArrayReader(Class<?> arrayType, Class<?> elementType, ValueReader vr) {
        super(arrayType);
        _elementType = elementType;
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
        CollectionBuilder b = r._collectionBuilder(null);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.emptyArray(_elementType);
        }
        Object value = _valueReader.read(r, p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.singletonArray(_elementType, value);
        }
        b = b.start().add(value);
        do {
            b = b.add(_valueReader.read(r, p));
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return b.buildArray(_elementType);
    }
    
    @Override
    public Object read(JSONReader r, JsonParser p) throws JacksonException {
        CollectionBuilder b = r._collectionBuilder(null);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.emptyArray(_elementType);
        }
        Object value = _valueReader.read(r, p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.singletonArray(_elementType, value);
        }
        b = b.start().add(value);
        do {
            b = b.add(_valueReader.read(r, p));
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return b.buildArray(_elementType);
    }
}
