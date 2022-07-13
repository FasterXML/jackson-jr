package tools.jackson.jr.ob.impl;

import java.util.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.MapBuilder;
import tools.jackson.jr.ob.api.ValueReader;

/**
 * Reader for typed {@link java.util.Map} values.
 */
public class MapReader extends ValueReader
{
    protected final Class<?> _mapType;
    protected final ValueReader _valueReader;

    public MapReader(Class<?> mapType, ValueReader vr) {
        super(mapType);
        // Some caveats: drop type if it's generic enough (aka "don't care")
        _mapType = (mapType == Map.class) ? null : mapType;
        _valueReader = vr;
    }

    @Override
    public Object readNext(JSONReader r, JsonParser p) throws JacksonException {
        if (p.nextToken() != JsonToken.START_OBJECT) {
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            throw JSONObjectException.from(p, "Unexpected token %s; should get START_OBJECT",
                    p.currentToken());
        }
        
        MapBuilder b = r._mapBuilder(_mapType);
        String propName0 = p.nextName();
        if (propName0 == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.emptyMap();
            }
            throw _reportWrongToken(p);
        }
        Object value = _valueReader.readNext(r, p);
        String propName = p.nextName();
        if (propName == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.singletonMap(propName0, value);
            }
            throw _reportWrongToken(p);
        }
        try {
            b = b.start().put(propName0, value);
            while (true) {
                b = b.put(propName, _valueReader.readNext(r, p));
                propName = p.nextName();
                if (propName == null) {
                    if (p.hasToken(JsonToken.END_OBJECT)) {
                        return b.build();
                    }
                    throw _reportWrongToken(p);
                }
            }
        } catch (IllegalArgumentException e) {
            throw JSONObjectException.from(p, e.getMessage());
        }
    }

    @Override
    public Object read(JSONReader r, JsonParser p) throws JacksonException {
        MapBuilder b = r._mapBuilder(_mapType);
        String propName0 = p.nextName();
        if (propName0 == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.emptyMap();
            }
            throw _reportWrongToken(p);
        }
        Object value = _valueReader.readNext(r, p);
        String propName = p.nextName();
        if (propName == null) {
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return b.singletonMap(propName0, value);
            }
            throw _reportWrongToken(p);
        }
        try {
            b = b.start().put(propName0, value);
            while (true) {
                b = b.put(propName, _valueReader.readNext(r, p));
                propName = p.nextName();
                if (propName == null) {
                    if (p.hasToken(JsonToken.END_OBJECT)) {
                        return b.build();
                    }
                    throw _reportWrongToken(p);
                }
            }
        } catch (IllegalArgumentException e) {
            throw JSONObjectException.from(p, e.getMessage());
        }
    }

    protected JSONObjectException _reportWrongToken(JsonParser p) {
        return JSONObjectException.from(p, "Unexpected token %s; should get FIELD_NAME or END_OBJECT",
                p.currentToken());
    }
}
