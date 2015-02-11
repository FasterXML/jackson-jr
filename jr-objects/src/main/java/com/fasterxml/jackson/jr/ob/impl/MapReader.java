package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

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
        // Some caveats: drop type if it's generic enough (aka "don't care")
        _mapType = (t == Map.class) ? null : t;
        _valueReader = vr;
    }
    
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException {
        MapBuilder b = r._mapBuilder(_mapType);
        String propName0 = p.nextFieldName();
        if (propName0 == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.emptyMap();
            }
            throw _reportProblem(p);
        }
        p.nextToken();
        Object value = _valueReader.read(r, p);
        String propName = p.nextFieldName();
        if (propName == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.singletonMap(propName0, value);
            }
            throw _reportProblem(p);
        }
        b = b.start().put(propName0, value);
        while (true) {
            p.nextToken();
            b = b.put(propName, _valueReader.read(r, p));
            propName = p.nextFieldName();
            if (propName == null) {
                if (p.hasToken(JsonToken.END_OBJECT)) {
                    return b.build();
                }
                throw _reportProblem(p);
            }
        }
    }

    protected IOException _reportProblem(JsonParser p) {
        return JSONObjectException.from(p, "Unexpected token "+p.getCurrentToken()+"; should get FIELD_NAME or END_OBJECT");
    }
}
