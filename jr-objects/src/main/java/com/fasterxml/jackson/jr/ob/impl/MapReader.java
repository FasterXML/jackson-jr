package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class MapReader extends ValueReader
{
    protected final Class<?> _mapType;
    protected final ValueReader _valueReader;

    public MapReader(Class<?> t, ValueReader vr) {
        // Some caveats: drop type if it's generic enough (aka "don't care")
        _mapType = (t == Map.class) ? null : t;
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException {
        MapBuilder b = r._mapBuilder(_mapType);
        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.emptyMap();
        }
        Object key = p.getCurrentName();
        Object value = _valueReader.read(r, p);

        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.singletonMap(key, value);
        }
        b = b.start().put(key, value);
        do {
            b = b.put(p.getCurrentName(), _valueReader.read(r, p));
        } while (p.nextValue() != JsonToken.END_OBJECT);
        return b.build();
    }
}