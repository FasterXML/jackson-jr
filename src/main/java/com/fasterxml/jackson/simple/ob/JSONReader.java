package com.fasterxml.jackson.simple.ob;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;

/**
 * Object that handles construction of simple Objects from JSON.
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct writing;
 * instead, per-call instance is created by calling
 * {@link #newReader}.
 */
public class JSONReader
{
    protected final static Object[] EMPTY_ARRAY = new Object[0];
    
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;

    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final JsonParser _parser;

    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating the default blueprint instance.
     */
    protected JSONReader(int features)
    {
//        this(features);
        _features = features;
        _parser = null;
    }

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    /*
    protected JSONReader(int features)
    {
        _features = features;
        _parser = null;
    }
    */

    /**
     * Constructor used for per-operation (non-blueprint) instance.
     */
    protected JSONReader(JSONReader base, JsonParser jp)
    {
        _features = base._features;
        _parser = jp;
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public final JSONReader withFeatures(int features) {
        if (_features == features) {
            return this;
        }
        return _with(features);
    }
    
    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONReader _with(int features)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSONReader(features);
    }

    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONReader newReader(JsonParser jp)
    {
        return new JSONReader(this, jp);
    }

    /*
    /**********************************************************************
    /* Public entry points for reading Simple objects from JSON
    /**********************************************************************
     */

    public final Object readValue() throws IOException, JsonProcessingException
    {
        return _readFromAny();
    }
    
    public Map<String,Object> readMap() throws IOException, JsonProcessingException
    {
        // !!! TODO
        return null;
    }

    public List<Object> readList() throws IOException, JsonProcessingException
    {
        // !!! TODO
        return null;
    }

    public Object[] readArray() throws IOException, JsonProcessingException
    {
        // !!! TODO
        return null;
    }

    /*
    /**********************************************************************
    /* Internal parse methods; overridable for custom coercions
    /**********************************************************************
     */

    @SuppressWarnings("incomplete-switch")
    protected Object _readFromAny() throws IOException, JsonProcessingException
    {
        switch (_parser.getCurrentToken()) {
        case VALUE_NULL:
            return nullForRootValue();
        case START_OBJECT:
            return _readFromObject();
        case START_ARRAY:
            return _readFromArray();
        case VALUE_STRING:
            return fromString(_parser.getText());
        case VALUE_NUMBER_INT:
            return _readFromInteger();
        case VALUE_NUMBER_FLOAT:
            return _readFromFloat();
        case VALUE_TRUE:
            return fromBoolean(true);
        case VALUE_FALSE:
            return fromBoolean(false);
        case VALUE_EMBEDDED_OBJECT:
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
    
    protected Object _readFromObject() throws IOException, JsonProcessingException
    {
        final JsonParser p = _parser;
        JsonToken t;
        final Map<Object,Object> result = new LinkedHashMap<Object,Object>();
        
        while ((t = p.nextValue()) != null && t != JsonToken.END_OBJECT) {
            Object key = fromKey(p.getCurrentName());
            Object value = _readFromAny();
            result.put(key, value);
        }
        return result;
    }

    protected Object _readFromArray() throws IOException, JsonProcessingException
    {
        final JsonParser p = _parser;
        JsonToken t;
        final List<Object> result = new ArrayList<Object>();
        
        while ((t = p.nextToken()) != null && t != JsonToken.END_ARRAY) {
            result.add(_readFromAny());
        }
        return result;
    }

    protected Object _readFromInteger() throws IOException, JsonProcessingException
    {
        switch (_parser.getNumberType()) {
        case INT:
            return Integer.valueOf(_parser.getIntValue());
        case LONG:
            return Long.valueOf(_parser.getLongValue());
        default:
        }
        return _parser.getBigIntegerValue();
    }

    protected Object _readFromFloat() throws IOException, JsonProcessingException
    {
        if (!Feature.USE_BIG_DECIMAL_FOR_FLOATS.isEnabled(_features)) {
            switch (_parser.getNumberType()) {
            case FLOAT:
                return Float.valueOf(_parser.getFloatValue());
            case DOUBLE:
                return Double.valueOf(_parser.getDoubleValue());
            default:
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
    protected Object fromNull() throws IOException, JsonProcessingException {
        return null;
    }
    
    /**
     * Method called to let implementation change a {@link java.lang.Boolean} value that has been
     * read from input.
     * Default implementation returns Boolean value as is.
     */
    protected Object fromBoolean(boolean b) throws IOException, JsonProcessingException {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Method called to let implementation change a key of an Object field
     * after being parsed from input.
     * Default implementation returns key as is.
     */
    protected Object fromKey(String key) throws IOException, JsonProcessingException {
        return key;
    }

    /**
     * Method called to let implementation change a {@link java.lang.String} value that has been
     * read from input.
     * Default implementation returns Boolean value as is.
     */
    protected Object fromString(String str) throws IOException, JsonProcessingException {
        // Nothing fancy, by default; return as is
        return str;
    }

    protected Object fromEmbedded(Object value) throws IOException, JsonProcessingException {
        return value;
    }
    
    public Object nullForRootValue() { return null; }

    public List<?> nullForRootList() { return null; }
    public Map<String,Object> nullForRootMap() { return null; }
    public Object[] nullForRootArray() {
        return null;
    }

    public List<?> emptyList() { return null; }
    public Map<String,Object> emptyMap() { return null; }
    public Object[] emptyArray() { // always safe to return empty array
        return EMPTY_ARRAY;
    }
    
    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */
}
