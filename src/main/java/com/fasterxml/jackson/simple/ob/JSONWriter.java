package com.fasterxml.jackson.simple.ob;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.simple.ob.impl.TypeDetector;

/**
 * Object that handles serialization of simple Objects into
 * JSON.
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct writing;
 * instead, per-call instance is created by calling
 * {@link #newWriter}.
 */
public class JSONWriter
{
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;
    
    /**
     * Object that is used to 
     */
    protected final TypeDetector _typeDetector;

    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final JsonGenerator _generator;
    
    /*
    /**********************************************************************
    /* Blueprint construction, configuration
    /**********************************************************************
     */

    /**
     * Constructor used for creating the default blueprint instance.
     */
    protected JSONWriter(int features)
    {
        this(features, TypeDetector.rootDetector());
    }

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    protected JSONWriter(int features, TypeDetector td)
    {
        _features = features;
        _typeDetector = td;
        _generator = null;
    }

    protected JSONWriter(JSONWriter base, JsonGenerator jgen)
    {
        _features = base._features;
        _typeDetector = base._typeDetector;
        _generator = jgen;
    }
    
    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONWriter newWriter(JsonGenerator jg)
    {
        return new JSONWriter(this, jg);
    }
    
    /*
    /**********************************************************************
    /* Public serialization methods
    /**********************************************************************
     */

    public void writeValue(Object value) throws IOException, JsonProcessingException
    {
        // !!! TODO
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
}
