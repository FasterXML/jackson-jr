package com.fasterxml.jackson.jr.ob.impl;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.CollectionBuilder;
import com.fasterxml.jackson.jr.ob.api.MapBuilder;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * Root-level helper object that handles initial delegation to actual
 * readers (which are {@link ValueReader}s), but does not handle
 * any of reading itself (despite name).
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct reading;
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

    protected final int _features;

    /**
     * Configured {@link TreeCodec} that is needed if values of type {@link TreeNode}
     * are to be read.
     */
    protected final TreeCodec _treeCodec;

    /**
     * Object that is used to find value readers dynamically.
     */
    protected final ValueReaderLocator _readerLocator;

    /**
     * Parser used by this reader instance.
     */
    protected final JsonParser _parser;

    /**
     * Minor performance optimization: {@code Object[1]} reused to avoid
     * Reflection having to allocate it for every "setter" call.
     *
     * @since 2.13
     */
    protected final Object[] _setterBuffer;

    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating the blueprint instances.
     */
    public JSONReader(CollectionBuilder lb, MapBuilder mb)
    {
        _features = 0;
        _readerLocator = null;
        _treeCodec = null;
        _collectionBuilder = lb;
        _mapBuilder = mb;
        _parser = null;
        _setterBuffer = null; // should NOT  be used on blueprint
    }

    /**
     * Constructor used for per-operation (non-blueprint) instance.
     */
    protected JSONReader(JSONReader base, int features,
            ValueReaderLocator loc, TreeCodec tc, JsonParser p)
    {
        _features = features;
        _readerLocator = loc.perOperationInstance(this, features);

        _treeCodec = tc;
        _collectionBuilder = base._collectionBuilder.newBuilder(features);
        _mapBuilder = base._mapBuilder.newBuilder(features);
        _parser = p;
        _setterBuffer = new Object[1];
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public JSONReader withCacheCheck(int features) {
        // 07-Jun-2019, tatu: No cache-dependant clearing needed... yet.
        return this;
    }

    public JSONReader with(MapBuilder mb) {
        if (_mapBuilder == mb) return this;
        return _with(_collectionBuilder, mb);
    }

    public JSONReader with(CollectionBuilder lb) {
        if (_collectionBuilder == lb) return this;
        return _with(lb, _mapBuilder);
    }

    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONReader _with(CollectionBuilder lb, MapBuilder mb)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSONReader(lb, mb);
    }

    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONReader perOperationInstance(int features,
            ValueReaderLocator loc, TreeCodec tc,
            JsonParser p)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONReader(this, features, loc, tc, p);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    /**
     * @since 2.8
     */
    public boolean arraysAsLists() {
        return JSON.Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS.isDisabled(_features);
    }

    /**
     * @since 2.11
     */
    public boolean isEnabled(JSON.Feature f) {
        return f.isEnabled(_features);
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
    public Object readValue() throws IOException {
        return AnyReader.std.read(this, _parser);
    }

    /**
     * Method for reading a JSON Object from input and building a {@link java.util.Map}
     * out of it. Note that if input does NOT contain a
     * JSON Object, {@link JSONObjectException} will be thrown.
     */
    public Map<String, Object> readMap() throws IOException {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_OBJECT) {
            throw JSONObjectException.from(_parser,
                    "Can not read a Map: expect to see START_OBJECT ('{'), instead got: "+ValueReader._tokenDesc(_parser));
        }
        return AnyReader.std.readFromObject(this, _parser, _mapBuilder);
    }

    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    public List<Object> readList() throws IOException {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read a List: expect to see START_ARRAY ('['), instead got: "+ValueReader._tokenDesc(_parser));
        }
        return (List<Object>) AnyReader.std.readCollectionFromArray(this, _parser, _collectionBuilder);
    }
    
    /**
     * Method for reading a JSON Array from input and building a <code>Object[]</code>
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    public Object[] readArray() throws IOException
    {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+ValueReader._tokenDesc(_parser));
        }
        return AnyReader.std.readArrayFromArray(this, _parser, _collectionBuilder);
    }

    /*
    /**********************************************************************
    /* Public entry points for reading (more) typed types
    /**********************************************************************
     */

    /**
     * Method for reading a JSON Object from input and building a Bean of
     * specified type out of it; Bean has to conform to standard Java Bean
     * specification by having setters for passing JSON Object properties.
     */
    @SuppressWarnings("unchecked")
    public <T> T readBean(Class<T> type) throws IOException {
        final Object ob = _readerLocator.findReader(type)
            .read(this, _parser);
        return (T) ob;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readArrayOf(Class<T> type) throws IOException {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read an array: expect to see START_ARRAY ('['), instead got: "+ValueReader._tokenDesc(_parser));
        }
        // NOTE: "array type" we give is incorrect, but should usually not matter
        // -- to fix would need to instantiate 0-element array, get that type
        return (T[]) new ArrayReader(type, type, _readerLocator.findReader(type))
                .read(this, _parser);
    }

    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it, binding values into specified {@code type}.
     * Note that if input does NOT contain a JSON Array, {@link JSONObjectException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readListOf(Class<T> type) throws IOException
    {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_ARRAY) {
            throw JSONObjectException.from(_parser,
                    "Can not read a List: expect to see START_ARRAY ('['), instead got: "+ValueReader._tokenDesc(_parser));
        }
        return (List<T>) new CollectionReader(List.class, _readerLocator.findReader(type))
                .read(this, _parser);
    }

    /**
     * Method for reading a JSON Object from input and building a {@link java.util.Map}
     * out of it, binding values into specified {@code type}.
     * Note that if input does NOT contain a JSON Object, {@link JSONObjectException} will be thrown.
     *
     * @since 2.10
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> readMapOf(Class<T> type) throws IOException
    {
        JsonToken t = _parser.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t != JsonToken.START_OBJECT) {
            throw JSONObjectException.from(_parser,
                    "Can not read a Map: expect to see START_OBJECT ('{'), instead got: "+ValueReader._tokenDesc(_parser));
        }
        return (Map<String, T>) new MapReader(Map.class, _readerLocator.findReader(type))
                .read(this, _parser);
    }

    /**
     * @since 2.11
     */
    public TreeNode readTree() throws IOException {
        if (_treeCodec == null) {
            throw new JSONObjectException("No `TreeCodec` specified: can not bind JSON into `TreeNode` types");
        }
        return _treeCodec.readTree(_parser);
    }

    /*
    /**********************************************************************
    /* Internal methods; overridable for custom coercions
    /**********************************************************************
     */

    protected MapBuilder _mapBuilder(Class<?> mapType) {
        return (mapType == null) ? _mapBuilder : _mapBuilder.newBuilder(mapType);
    }

    protected CollectionBuilder _collectionBuilder(Class<?> collType) {
        return (collType == null) ? _collectionBuilder : _collectionBuilder.newBuilder(collType);
    }
}
