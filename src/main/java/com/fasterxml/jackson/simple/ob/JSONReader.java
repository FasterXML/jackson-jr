package com.fasterxml.jackson.simple.ob;

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
    /* Blueprint construction, configuration
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

    protected JSONReader(JSONReader base, JsonParser jp)
    {
        _features = base._features;
        _parser = jp;
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
    /* Public serialization methods
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
}
