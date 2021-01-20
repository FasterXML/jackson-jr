package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * API and base class for all "deserializer" implementations used to actually read
 * values of Java types from (JSON) input.
 */
public abstract class ValueReader
{
    /**
     * Type of values this reader will read
     */
    protected final Class<?> _valueType;

    protected ValueReader(Class<?> valueType) {
        _valueType = valueType;
    }

    /*
    /**********************************************************************
    /* Basic API to implement for actual read operations
    /**********************************************************************
     */

    public abstract Object read(JSONReader reader, JsonParser p) throws JacksonException;

    /**
     * Method called to deserialize value of type supported by this reader, using
     * given parser. Parser is not yet positioned to the (first) token
     * of the value to read and needs to be advanced.
     *<p>
     * Default implementation simply calls `p.nextToken()` first, then calls
     * {#link {@link #read(JSONReader, JsonParser)}, but some implementations
     * may decide to implement this differently to use (slightly) more efficient
     * accessor in {@link JsonParser}, like {@link JsonParser#nextIntValue(int)}.
     *
     * @param reader Context object that allows calling other read methods for contained
     *     values of different types (for example for collection readers).
     * @param p Underlying parser used for reading decoded token stream
     */
    public Object readNext(JSONReader reader, JsonParser p) throws JacksonException {
        p.nextToken();
        return read(reader, p);
    }

    /*
    /**********************************************************************
    /* Minimal metadata
    /**********************************************************************
     */

    /**
     * Accessor for non-generic (type-erased) type of values this reader
     * produces from input.
     *
     * @since 2.10
     */
    public Class<?> valueType() {
        return _valueType;
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    public static String _tokenDesc(JsonParser p) {
        return _tokenDesc(p, p.currentToken());
    }
    
    protected static String _tokenDesc(JsonParser p, JsonToken t) {
        if (t == null) {
            return "NULL";
        }
        switch (t) {
        case FIELD_NAME:
            return "JSON Field name '"+p.currentName()+"'";
        case START_ARRAY:
            return "JSON Array";
        case START_OBJECT:
            return "JSON Object";
        case VALUE_FALSE:
            return "'false'";
        case VALUE_NULL:
            return "'null'";
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
            return "JSON Number";
        case VALUE_STRING:
            return "JSON String";
        case VALUE_TRUE:
            return "'true'";
        default:
            return t.toString();
        }
    }
}
