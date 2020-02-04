package com.fasterxml.jackson.jr.ob.impl;

import static com.fasterxml.jackson.core.JsonTokenId.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParser.NumberType;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.CollectionBuilder;
import com.fasterxml.jackson.jr.ob.api.MapBuilder;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * {@link ValueReader} used for "untyped" values; ones that are bound
 * to whatever {@link java.lang.Object} is the natural mapping to JSON
 * value that parser currently points to
 */
public class AnyReader extends ValueReader
{
    public final static AnyReader std = new AnyReader();

    public AnyReader() { super(Object.class); }
    
    @Override
    public Object readNext(JSONReader r, JsonParser p) throws IOException
    {
        p.nextToken();
        return read(r, p);
    }
    
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException
    {
        switch (p.currentTokenId()) {
        case ID_NULL:
            return null;
        case ID_START_OBJECT:
            return readFromObject(r, p, r._mapBuilder);
        case ID_START_ARRAY:
            if (r.arraysAsLists()) {
                return readCollectionFromArray(r, p, r._collectionBuilder);
            }
            return readArrayFromArray(r, p, r._collectionBuilder);
        case ID_STRING:
            return fromString(p.getText());
        case ID_NUMBER_INT:
            {
                NumberType n = p.getNumberType();
                if (n == NumberType.INT) {
                    return Integer.valueOf(p.getIntValue());
                }
                if (n == NumberType.LONG) {
                    return Long.valueOf(p.getLongValue());
                }
                return p.getBigIntegerValue();
            }
        case ID_NUMBER_FLOAT:
            if (!JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS.isEnabled(r._features)) {
                NumberType n = p.getNumberType();
                if (n == NumberType.FLOAT) {
                    return Float.valueOf(p.getFloatValue());
                }
                if (n == NumberType.DOUBLE) {
                    return Double.valueOf(p.getDoubleValue());
                }
            }
            return p.getDecimalValue();
        case ID_TRUE:
            return fromBoolean(true);
        case ID_FALSE:
            return fromBoolean(false);
        case ID_EMBEDDED_OBJECT:
            return fromEmbedded(p.getEmbeddedObject());

            // Others are error cases...
            /*
        default:
        case END_ARRAY:
        case END_OBJECT:
        case FIELD_NAME:
        case NOT_AVAILABLE:
        */
        }
        throw JSONObjectException.from(p, "Unexpected value token: "+_tokenDesc(p));
    }

    public Map<String, Object> readFromObject(JSONReader r, JsonParser p, MapBuilder b) throws IOException
    {
        // First, a minor optimization for empty Maps
        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.emptyMap();
        }
        // and another for singletons...
        String key = fromKey(p.getCurrentName());
        Object value = read(r, p);

        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.singletonMap(key, value);
        }

        // but then it's loop-de-loop
        try {
            b = b.start().put(key, value);
            do {
                b = b.put(fromKey(p.getCurrentName()), read(r, p));
            } while (p.nextValue() != JsonToken.END_OBJECT);
        } catch (IllegalArgumentException e) {
            throw JSONObjectException.from(p, e.getMessage());
        }
        return b.build();
    }

    public Object[] readArrayFromArray(JSONReader r, JsonParser p, CollectionBuilder b) throws IOException
    {
        // First two special cases; empty, single-element
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.emptyArray();
        }
        Object value = read(r, p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.singletonArray(value);
        }
        try {
            b = b.start().add(value);
            do {
                b = b.add(read(r, p));
            } while (p.nextToken() != JsonToken.END_ARRAY);
            return b.buildArray();
        } catch (IllegalArgumentException e) {
            throw JSONObjectException.from(p, e.getMessage());
        }
    }

    public Collection<Object> readCollectionFromArray(JSONReader r, JsonParser p, CollectionBuilder b) throws IOException
    {
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.emptyCollection();
        }
        Object value = read(r, p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return b.singletonCollection(value);
        }
        try {
            b = b.start().add(value);
            do {
                b = b.add(read(r, p));
            } while (p.nextToken() != JsonToken.END_ARRAY);
            return b.buildCollection();
        } catch (IllegalArgumentException e) {
            throw JSONObjectException.from(p, e.getMessage());
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, simple scalar conversions
    /**********************************************************************
     */
    
    /**
     * Method called to let implementation change a null value that has been
     * read from input.
     * Default implementation returns null as is.
     */
    protected Object fromNull() throws IOException {
        return null;
    }

    /**
     * Method called to let implementation change a {@link java.lang.Boolean} value that has been
     * read from input.
     * Default implementation returns Boolean value as is.
     */
    protected Object fromBoolean(boolean b) throws IOException {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Method called to let implementation change a key of an Object field
     * after being parsed from input.
     * Default implementation returns key as is.
     */
    protected String fromKey(String key) throws IOException {
        return key;
    }

    /**
     * Method called to let implementation change a {@link java.lang.String} value that has been
     * read from input.
     * Default implementation returns String value as is.
     */
    protected Object fromString(String str) throws IOException {
        // Nothing fancy, by default; return as is
        return str;
    }

    protected Object fromEmbedded(Object value) throws IOException {
        return value;
    }
}
