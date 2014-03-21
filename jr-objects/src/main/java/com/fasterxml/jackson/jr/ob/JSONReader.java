package com.fasterxml.jackson.jr.ob;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.impl.BeanDefinition;
import com.fasterxml.jackson.jr.ob.impl.BeanProperty;
import com.fasterxml.jackson.jr.ob.impl.CollectionBuilder;
import com.fasterxml.jackson.jr.ob.impl.MapBuilder;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

import static com.fasterxml.jackson.core.JsonTokenId.*;
import static com.fasterxml.jackson.jr.ob.impl.TypeDetector.*;

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

    protected final TreeCodec _treeCodec;
    
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
    protected final CollectionBuilder _collectionBuilder;
    
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
    protected JSONReader(int features, TypeDetector td, TreeCodec treeCodec,
            CollectionBuilder lb, MapBuilder mb)
    {
        _features = features;
        _typeDetector = td;
        _treeCodec = treeCodec;
        _collectionBuilder = lb;
        _mapBuilder = mb;
        _arraysAsLists = Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS.isDisabled(features);
        _parser = null;
    }

    /**
     * Constructor used for per-operation (non-blueprint) instance.
     */
    protected JSONReader(JSONReader base, JsonParser jp)
    {
        int features = base._features;
        _features = features;
        _typeDetector = base._typeDetector.perOperationInstance(features);
        _treeCodec = base._treeCodec;
        _collectionBuilder = base._collectionBuilder.newBuilder(features);
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
        return _with(features, _typeDetector, _treeCodec, _collectionBuilder, _mapBuilder);
    }

    public final JSONReader with(MapBuilder mb) {
        if (_mapBuilder == mb) return this;
        return _with(_features, _typeDetector, _treeCodec, _collectionBuilder, mb);
    }

    public final JSONReader with(CollectionBuilder lb) {
        if (_collectionBuilder == lb) return this;
        return _with(_features, _typeDetector, _treeCodec, lb, _mapBuilder);
    }
    
    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONReader _with(int features,
            TypeDetector td, TreeCodec tc, CollectionBuilder lb, MapBuilder mb)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSONReader(features, td, tc, lb, mb);
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
     * JSON Object, {@link JSONObjectException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public Map<Object,Object> readMap() throws IOException {
        if (_parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JSONObjectException.from(_parser,
                    "Can not read a Map: expect to see START_OBJECT ('{'), instead got: "+_tokenDesc());
        }
        return (Map<Object,Object>) _readFromObject(_mapBuilder);
    }
    
    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public List<Object> readList() throws IOException {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read a List: expect to see START_ARRAY ('['), instead got: "+_tokenDesc());
        }
        return (List<Object>) _readFromArray(_collectionBuilder, true);
    }

    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readListOf(Class<T> type) throws IOException
    {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read a List: expect to see START_ARRAY ('['), instead got: "+_tokenDesc());
        }
        return (List<T>) _readFromArray(_collectionBuilder, type, true);
    }
    
    /**
     * Method for reading a JSON Array from input and building a <code>Object[]</code>
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    public Object[] readArray() throws IOException, JsonProcessingException
    {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc());
        }
        return (Object[]) _readFromArray(_collectionBuilder, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readArrayOf(Class<T> type) throws IOException, JsonProcessingException {
        if (_parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc());
        }
        return (T[]) _readFromArray(_collectionBuilder, type, false);
    }

    /**
     * Method for reading a JSON Object from input and building a Bean of
     * specified type out of it; Bean has to conform to standard Java Bean
     * specification by having setters for passing JSON Object properties.
     */
    @SuppressWarnings("unchecked")
    public <T> T readBean(Class<T> type) throws IOException, JsonProcessingException {
        return (T) _readBean(type, _typeDetector.findFullType(type));
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
            return _readFromObject(_mapBuilder);
        case ID_START_ARRAY:
            return _readFromArray(_collectionBuilder, _arraysAsLists);
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
        throw JSONObjectException.from(_parser, "Unexpected value token: "+_tokenDesc());
    }
    
    protected Object _readBean(Class<?> type, int typeId) throws IOException
    {
        if (typeId < 0) { // actual bean types
            BeanDefinition def = _typeDetector.getBeanDefinition(typeId);
            JsonToken t = _parser.getCurrentToken();

            try {
                Object bean = null;
                switch (t) {
                case VALUE_STRING:
                    bean = def.create(_parser.getText());
                    break;
                case VALUE_NUMBER_INT:
                    bean = def.create(_parser.getLongValue());
                    break;
                case START_OBJECT:
                    {
                        bean = def.create();
                        for (; (t = _parser.nextToken()) == JsonToken.FIELD_NAME; ) {
                            String fieldName = _parser.getCurrentName();
                            BeanProperty prop = def.findProperty(fieldName);
                            if (prop == null) {
                                if (JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY.isEnabled(_features)) {
                                    throw JSONObjectException.from(_parser, "Unrecognized JSON property '"
                                            +fieldName+"' for Bean type "+type.getName());
                                }
                                _parser.nextToken();
                                _parser.skipChildren();
                                continue;
                            }
                            _parser.nextToken();
                            Class<?> rawType = prop.getType();
                            int propType = prop.getTypeId();
                            // need to dynamically resolve bean type refs
                            if (propType == TypeDetector.SER_UNKNOWN) {
                                propType = _typeDetector.findFullType(rawType);
                                if (propType != TypeDetector.SER_UNKNOWN) { 
                                    prop.overridTypeId(propType);
                                }
                            }
                            Object value = _readBean(rawType, propType);
                            prop.setValueFor(bean, value);
                        }
                    }
                    break;
                default:
                }
                if (bean != null) {
                    return bean;
                }
            } catch (JSONObjectException e) {
                throw e;
            } catch (Exception e) {
                throw JSONObjectException.from(_parser, "Failed to create an instance of "
                        +type.getName()+" due to ("+e.getClass().getName()+"): "+e.getMessage(),
                        e);
            }
        } else switch (typeId) {
        // Structured types:
        case SER_MAP:
        {
            MapBuilder b = _mapBuilder;
            if (type != Map.class) {
                b = b.newBuilder(type);
            }
            return _readFromObject(b);
        }
            
        case SER_LIST:
        case SER_COLLECTION:
        {
            CollectionBuilder b = _collectionBuilder;
            if (type != List.class && type != Collection.class) {
                b = b.newBuilder(type);
            }
            return _readFromArray(b, true);
        }

        case SER_OBJECT_ARRAY:
            return _readFromArray(_collectionBuilder, false);

        case SER_INT_ARRAY:
            return _readIntArray();

        case SER_TREE_NODE:
            return _treeCodec().readTree(_parser);

        // Textual types, related:
        case SER_STRING:
        case SER_CHARACTER_SEQUENCE:
            return _parser.getValueAsString();
        case SER_CHAR_ARRAY:
            return _parser.getValueAsString().toCharArray();
        case SER_BYTE_ARRAY:
            return _readBinary();

        // Number types:
            
        case SER_NUMBER_FLOAT: // fall through
            return Float.valueOf((float) _parser.getValueAsDouble());
        case SER_NUMBER_DOUBLE:
            return _parser.getValueAsDouble();

        case SER_NUMBER_BYTE: // fall through
            return (byte) _parser.getValueAsInt();
            
        case SER_NUMBER_SHORT: // fall through
            return (short) _parser.getValueAsInt();
        case SER_NUMBER_INTEGER:
            return _parser.getValueAsInt();
        case SER_NUMBER_LONG:
            return _parser.getValueAsLong();

        case SER_NUMBER_BIG_DECIMAL:
            return _parser.getDecimalValue();

        case SER_NUMBER_BIG_INTEGER:
            return _parser.getBigIntegerValue();

        // Other scalar types:

        case SER_BOOLEAN:
            return _parser.getValueAsBoolean();
            
        case SER_CHAR:
            {
                String str = _parser.getValueAsString();
                return (str == null || str.isEmpty()) ? ' ' : str.charAt(0);
            }
            
        case SER_CALENDAR:
            {
                long l = _fetchLong(type, typeId);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(l);
                return cal;
            }

        case SER_DATE:
            return new Date(_fetchLong(type, typeId));

        case SER_ENUM:
            return _readEnum(type);
    
        case SER_CLASS:
        {
            String v = _parser.getValueAsString();
            try {
                return Class.forName(v);
            } catch (Exception e) {
                throw new JSONObjectException("Failed to bind java.lang.Class from value '"+v+"'");
            }
        }
        case SER_FILE:
            return new File(_parser.getValueAsString());
        case SER_UUID:
            return UUID.fromString(_parser.getValueAsString());
        case SER_URL:
            return new URL(_parser.getValueAsString());
        case SER_URI:
            return URI.create(_parser.getValueAsString());
        }
        throw JSONObjectException.from(_parser,
                "Can not create a "+type.getName()+" instance out of "+_tokenDesc());
    }

    protected Object _readFromObject(MapBuilder b) throws IOException
    {
        final JsonParser p = _parser;
        
        // First, a minor optimization for empty Maps
        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.emptyMap();
        }
        // and another for singletons...
        Object key = fromKey(p.getCurrentName());
        Object value = _readFromAny();

        if (p.nextValue() == JsonToken.END_OBJECT) {
            return b.singletonMap(key, value);
        }

        // but then it's loop-de-loop
        b = b.start().put(key, value);
        do {
            b = b.put(fromKey(p.getCurrentName()), _readFromAny());
        } while (p.nextValue() != JsonToken.END_OBJECT);
        return b.build();
    }

    /**
     * @param asList Whether to bind into a {@link java.util.List} (true), or
     *    <code>Object[]</code> (false)
     */
    protected Object _readFromArray(CollectionBuilder b, boolean asList) throws IOException
    {
        final JsonParser p = _parser;
        // First two special cases; empty, single-element
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ? b.emptyCollection() : b.emptyArray();
        }
        Object value = _readFromAny();
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ?  b.singletonCollection(value) : b.singletonArray(value);
        }
        // otherwise, loop
        b = b.start().add(value);
        do {
            b = b.add(_readFromAny());
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return asList ? b.buildCollection() : b.buildArray();
    }

    @SuppressWarnings("unchecked")
    protected <T> Object _readFromArray(CollectionBuilder b, Class<T> type, boolean asList) throws IOException
    {
        final JsonParser p = _parser;
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ? b.emptyCollection() : b.emptyArray(type);
        }
        int typeId = _typeDetector.findFullType(type);
        Object value = _readBean(type, typeId);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return asList ?  b.singletonCollection(value) : b.singletonArray(type, (T) value);
        }
        // otherwise, loop
        b = b.start().add(value);
        do {
            b = b.add(_readBean(type, typeId));
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return asList ? b.buildCollection() : b.buildArray(type);
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
    /* Read methods for scalars
    /**********************************************************************
     */

    protected byte[] _readBinary() throws IOException {
        return _parser.getBinaryValue();
    }
    
    protected int[] _readIntArray() throws IOException
    {
        // !!! TODO
        throw new JSONObjectException("Reading of int[] not yet implemented");
    }

    protected Object _readEnum(Class<?> type) throws IOException
    {
        Object[] enums = type.getEnumConstants();
        JsonToken t = _parser.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            int ix = _parser.getIntValue();
            if (ix < 0 || ix >= enums.length) {
                throw new JSONObjectException("Failed to bind Enum "+type.getName()+" with index "+ix
                        +" (has "+enums.length+" values)");
            }
            return enums[ix];
        }
        String v = _parser.getValueAsString().trim();

        // !!! TODO: 20-Mar-2014, tatu: Need an efficient way to handle this, construct
        //   EnumResolver etc
        for (Object e : enums) {
            if (v.equals(e.toString())) {
                return e;
            }
        }
        throw new JSONObjectException("Failed to bind Enum "+type.getName()+" from value '"+v+"'");
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
     * Default implementation returns String value as is.
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

    protected String _tokenDesc() throws IOException {
        return _tokenDesc(_parser.getCurrentToken());
    }

    protected String _tokenDesc(JsonToken t) throws IOException {
        if (t == null) {
            return "NULL";
        }
        switch (t) {
        case FIELD_NAME:
            return "JSON Field name '"+_parser.getCurrentName()+"'";
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

    protected TreeCodec _treeCodec() throws JSONObjectException {
        if (_treeCodec == null) {
            throw new JSONObjectException("No TreeCodec specified: can not bind JSON into TreeNode types");
        }
        return _treeCodec;
        
    }

    protected long _fetchLong(Class<?> type, int typeId) throws IOException
    {
        JsonToken t = _parser.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return _parser.getLongValue();
        }
        throw JSONObjectException.from(_parser, "Can not get long numeric value from JSON (to construct "
                +type.getName()+") from "+_tokenDesc(t));
    }
}
