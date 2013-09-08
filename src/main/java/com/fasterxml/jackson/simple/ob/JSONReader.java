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

    public Object readValue() throws IOException, JsonProcessingException
    {
        // !!! TODO
        return null;
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

    /*
    /**********************************************************************
    /* Internal parse methods
    /**********************************************************************
     */
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
}
