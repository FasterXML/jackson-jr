package com.fasterxml.jackson.jr.ob;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.impl.ListBuilder;
import com.fasterxml.jackson.jr.ob.impl.MapBuilder;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

import static com.fasterxml.jackson.core.JsonTokenId.*;

/**
 * Object that handles construction of simple Objects from JSON.
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct writing;
 * instead, per-call instance is created by calling
 * {@link #perOperationInstance}.
 */
public class JSONReader
{
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;

    protected final boolean _arraysAsLists;

    /**
     * Object that is used to resolve types of values dynamically.
     */
    protected final TypeDetector _typeDetector;
    
    /**
     * Handler that takes care of constructing {@link java.util.Map}s as needed
     */
    protected final MapBuilder _mapBuilder;

    /**
     * Handler that takes care of constructing {@link java.util.Map}s as needed
     */
    protected final ListBuilder _listBuilder;
    
    /*
    /**********************************************************************
    /* Instance config, state
    /**********************************************************************
     */

    protected final JsonParser _parser;

    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating the blueprint instances.
     */
    protected JSONReader(int features, TypeDetector td,
            ListBuilder lb, MapBuilder mb)
    {
        _features = features;
        _typeDetector = td;
        _arraysAsLists = Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS.isDisabled(features);
        _parser = null;
        _listBuilder = lb;
        _mapBuilder = mb;
    }

    /**
     * Constructor used for per-operation (non-blueprint) instance.
     */
    protected JSONReader(JSONReader base, JsonParser jp)
    {
        int features = base._features;
        _features = features;
        _typeDetector = base._typeDetector;
        _listBuilder = base._listBuilder.newBuilder(features);
        _mapBuilder = base._mapBuilder.newBuilder(features);
        _arraysAsLists = base._arraysAsLists;
        _parser = jp;
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public final JSONReader withFeatures(int features)
    {
        if (_features == features) {
            return this;
        }
        return _with(features, _typeDetector, _listBuilder, _mapBuilder);
    }

    public final JSONReader with(MapBuilder mb) {
        if (_mapBuilder == mb) return this;
        return _with(_features, _typeDetector, _listBuilder, mb);
    }

    public final JSONReader with(ListBuilder lb) {
        if (_listBuilder == lb) return this;
        return _with(_features, _typeDetector, lb, _mapBuilder);
    }
    
    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONReader _with(int features,
            TypeDetector td, ListBuilder lb, MapBuilder mb)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSONReader(features, td, lb, mb);
    }

    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONReader perOperationInstance(JsonParser jp)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONReader(this, jp);
    }

    /*
    /**********************************************************************
    /* Public entry points for reading Simple objects from JSON
    /**********************************************************************
     */

    /**
     * Method for reading a "simple" Object of type indicated by JSON
     * content: {@link java.util.Map} for JSON Object, {@link java.util.Map}
     * for JSON Array (or, <code>Object[]</code> if so configured),
     * {@link java.lang.String} for JSON String value and so on.
     */
    public final Object readValue() throws IOException {
        return _readFromAny();
    }

    /**
     * Method for reading a JSON Object from input and building a {@link java.util.Map}
     * out of it. Note that if input does NOT contain a
     * JSON Object, {@link JsonProcessingException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public Map<Object,Object> readMap() throws IOException, JsonProcessingException
    {
        if (_parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JSONObjectException.from(_parser,
                    "Can not read a Map: expect to see START_OBJECT ('{'), instead got: "+_tokenDesc(_parser));
        }
        return (Map<Object,Object>) _readFromObject();
    }
    
    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JsonProcessingException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public List<Object> readList() throws IOException, JsonProcessingException
    {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read a List: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
        }
        return (List<Object>) _readFromArray(true);
    }

    /**
     * Method for reading a JSON Array from input and building a <code>Object[]</code>
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JsonProcessingException} will be thrown.
     */
    public Object[] readArray() throws IOException, JsonProcessingException
    {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
        }
        return (Object[]) _readFromArray(false);
    }

    /**
     * Method for reading a JSON Object from input and building a Bean of
     * specified type out of it; Bean has to conform to standard Java Bean
     * specification by having setters for passing JSON Object properties.
     * Note that if input does NOT contain a
     * JSON Object, {@link JsonProcessingException} will be thrown.
     */
    public <T> T readBean(Class<T> type) throws IOException, JsonProcessingException
    {
        if (_parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
        }
        
        // !!! TODO

        return null;
    }
    
    /*
    /**********************************************************************
    /* Internal parse methods; overridable for custom coercions
    /**********************************************************************
     */

    protected Object _readFromAny() throws IOException
    {
        JsonToken t = _parser.getCurrentToken();
        int id = (t == null) ? ID_NO_TOKEN : t.id();
        switch (id) {
        case ID_NULL:
            return nullForRootValue();
        case ID_START_OBJECT:
            return _readFromObject();
        case ID_START_ARRAY:
            return _readFromArray(_arraysAsLists);
        case ID_STRING:
            return fromString(_parser.getText());
        case ID_NUMBER_INT:
            return _readFromInteger();
        case ID_NUMBER_FLOAT:
            return _readFromFloat();
        case ID_TRUE:
            return fromBoolean(true);
        case ID_FALSE:
            return fromBoolean(false);
        case ID_EMBEDDED_OBJECT:
            return fromEmbedded(_parser.getEmbeddedObject());

            // Others are error cases...
            /*
        default:
        case END_ARRAY:
        case END_OBJECT:
        case FIELD_NAME:
        case NOT_AVAILABLE:
        */
        }
        throw JSONObjectException.from(_parser, "Unexpected value token: "+_parser.getCurrentToken());
    }

    protected Object _readFromObject() throws IOException
    {
        final JsonParser p = _parser;

        // First, a minor optimization for empty Maps
        if (p.nextValue() == JsonToken.END_OBJECT) {
            return _mapBuilder.emptyMap();
        }
        // and another for singletons...
        Object key = fromKey(p.getCurrentName());
        Object value = _readFromAny();

        if (p.nextValue() == JsonToken.END_OBJECT) {
            return _mapBuilder.singletonMap(key, value);
        }

        // but then it's loop-de-loop
        MapBuilder b = _mapBuilder.start().put(key, value);
        do {
            b = b.put(fromKey(p.getCurrentName()), _readFromAny());
        } while (p.nextValue() != JsonToken.END_OBJECT);
        return b.build();
    }

    /**
     * @param asList Whether to bind into a {@link java.util.List} (true), or
     *    <code>Object[]</code> (false)
     */
    protected Object _readFromArray(boolean asList) throws IOException
    {
        final JsonParser p = _parser;
        // First two special cases; empty, single-element
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ? _listBuilder.emptyList() : _listBuilder.emptyArray();
        }
        Object value = _readFromAny();
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ?  _listBuilder.singletonList(value) : _listBuilder.singletonList(value);
        }
        // otherwise, loop
        ListBuilder b = _listBuilder.start().add(value);
        do {
            b = b.add(_readFromAny());
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return asList ? b.buildList() : b.buildArray();
    }

    protected Object _readFromInteger() throws IOException
    {
        NumberType t = _parser.getNumberType();
        if (t == NumberType.INT) {
            return Integer.valueOf(_parser.getIntValue());
        }
        if (t == NumberType.LONG) {
            return Long.valueOf(_parser.getLongValue());
        }
        return _parser.getBigIntegerValue();
    }

    protected Object _readFromFloat() throws IOException
    {
        if (!JSON.Feature.USE_BIG_DECIMAL_FOR_FLOATS.isEnabled(_features)) {
            NumberType t = _parser.getNumberType();
            if (t == NumberType.FLOAT) {
                return Float.valueOf(_parser.getFloatValue());
            }
            if (t == NumberType.DOUBLE) {
                return Double.valueOf(_parser.getDoubleValue());
            }
        }
        return _parser.getDecimalValue();
    }
    
    /*
    /**********************************************************************
    /* Internal methods for handling specific values, possible overrides, conversions
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
    protected Object fromKey(String key) throws IOException {
        return key;
    }

    /**
     * Method called to let implementation change a {@link java.lang.String} value that has been
     * read from input.
     * Default implementation returns Boolean value as is.
     */
    protected Object fromString(String str) throws IOException {
        // Nothing fancy, by default; return as is
        return str;
    }

    protected Object fromEmbedded(Object value) throws IOException {
        return value;
    }
    
    public Object nullForRootValue() { return null; }

    public List<?> nullForRootList() { return null; }
    public Map<Object,Object> nullForRootMap() { return null; }
    public Object[] nullForRootArray() {
        return null;
    }

    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    protected String _tokenDesc(JsonParser p) throws IOException {
        JsonToken t = _parser.getCurrentToken();
        if (t == null) {
            return "NULL";
        }
        switch (t) {
        case FIELD_NAME:
            return "JSON Field name '"+p.getCurrentName()+"'";
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
