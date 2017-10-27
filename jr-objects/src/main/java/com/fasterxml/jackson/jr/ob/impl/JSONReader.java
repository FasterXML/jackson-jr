package com.fasterxml.jackson.jr.ob.impl;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

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
    extends ValueReader // just to get convenience methods
{
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;

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
    public JSONReader(int features, TypeDetector td, TreeCodec treeCodec,
            CollectionBuilder lb, MapBuilder mb)
    {
        _features = features;
        _typeDetector = td;
        _treeCodec = treeCodec;
        _collectionBuilder = lb;
        _mapBuilder = mb;
        _parser = null;
    }

    /**
     * Constructor used for per-operation (non-blueprint) instance.
     */
    protected JSONReader(JSONReader base, int features, TypeDetector td, JsonParser p)
    {
        _features = features;
        _typeDetector = td;
        _treeCodec = base._treeCodec;
        _collectionBuilder = base._collectionBuilder.newBuilder(features);
        _mapBuilder = base._mapBuilder.newBuilder(features);
        _parser = p;
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        // never to be called for this instance
        throw new UnsupportedOperationException();
    }

    @Override
    public Object readNext(JSONReader reader, JsonParser p) throws IOException {
        // never to be called for this instance
        throw new UnsupportedOperationException();
    }
    
    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public JSONReader withFeatures(int features)
    {
        if (_features == features) {
            return this;
        }
        return _with(features, _typeDetector, _treeCodec, _collectionBuilder, _mapBuilder);
    }

    public JSONReader with(MapBuilder mb) {
        if (_mapBuilder == mb) return this;
        return _with(_features, _typeDetector, _treeCodec, _collectionBuilder, mb);
    }

    public JSONReader with(CollectionBuilder lb) {
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

    public JSONReader perOperationInstance(int features, JsonParser p)
    {
        if (getClass() != JSONReader.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONReader(this, features,
                _typeDetector.perOperationInstance(features), p);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    public boolean arraysAsLists() {
        return Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS.isDisabled(_features);
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
    public Map<Object,Object> readMap() throws IOException {
        if (_parser.isExpectedStartObjectToken()) {
            return AnyReader.std.readFromObject(this, _parser, _mapBuilder);
        }
        if (_parser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        throw JSONObjectException.from(_parser,
                "Can not read a Map: expect to see START_OBJECT ('{'), instead got: "+_tokenDesc(_parser));
    }

    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    public List<Object> readList() throws IOException {
        if (_parser.isExpectedStartArrayToken()) {
            return (List<Object>) AnyReader.std.readCollectionFromArray(this, _parser, _collectionBuilder);
        }
        if (_parser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        throw JSONObjectException.from(_parser,
                "Can not read a List: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
    }

    /**
     * Method for reading a JSON Array from input and building a <code>Object[]</code>
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    public Object[] readArray() throws IOException
    {
        if (_parser.isExpectedStartArrayToken()) {
            return AnyReader.std.readArrayFromArray(this, _parser, _collectionBuilder);
        }
        if (_parser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        throw JSONObjectException.from(_parser,
                "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
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
        ValueReader vr = _typeDetector.findReader(type);
        return (T) vr.read(this, _parser);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readArrayOf(Class<T> type) throws IOException {
        if (_parser.isExpectedStartArrayToken()) {
            return (T[]) new ArrayReader(type, _typeDetector.findReader(type)).read(this, _parser);
        }
        if (_parser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        throw JSONObjectException.from(_parser,
                "Can not read an array: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
    }

    /**
     * Method for reading a JSON Array from input and building a {@link java.util.List}
     * out of it. Note that if input does NOT contain a
     * JSON Array, {@link JSONObjectException} will be thrown.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readListOf(Class<T> type) throws IOException
    {
        if (_parser.isExpectedStartArrayToken()) {
            return (List<T>) new CollectionReader(List.class, _typeDetector.findReader(type)).read(this, _parser);
        }
        if (_parser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        throw JSONObjectException.from(_parser,
                "Can not read a List: expect to see START_ARRAY ('['), instead got: "+_tokenDesc(_parser));
    }

    /*
    /**********************************************************************
    /* Internal methods; overridable for custom coercions
    /**********************************************************************
     */

    protected TreeCodec _treeCodec() throws JSONObjectException {
        if (_treeCodec == null) {
            throw new JSONObjectException("No TreeCodec specified: can not bind JSON into TreeNode types");
        }
        return _treeCodec;
    }

    protected MapBuilder _mapBuilder(Class<?> mapType) {
        return (mapType == null) ? _mapBuilder : _mapBuilder.newBuilder(mapType);
    }

    protected CollectionBuilder _collectionBuilder(Class<?> collType) {
        return (collType == null) ? _collectionBuilder : _collectionBuilder.newBuilder(collType);
    }
}
