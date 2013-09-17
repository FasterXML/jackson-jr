package com.fasterxml.jackson.simple.ob;

import java.io.Closeable;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.simple.ob.impl.SequenceComposer;

/**
 * Root-level composer object that acts as streaming "builder"
 * object, using an underlying {@link com.fasterxml.jackson.core.JsonGenerator} object.
 * This is similar to {@link com.fasterxml.jackson.simple.ob.impl.ArrayComposer}, but does not
 * have parent composer (so no <code>end()</code> method),
 * but does implement {@link java.io.Closeable}
 */
public class JSONComposer
    extends SequenceComposer<JSONComposer>
    implements Closeable
{
    public JSONComposer(JsonGenerator jgen)
    {
        super(jgen);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    @Override
    public void close() throws IOException, JsonProcessingException
    {
        if (_open) {
            _closeChild();
            _open = false;
            _generator.close();
        }
    }
    
    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */
    
    @Override
    protected JSONComposer _start() throws IOException, JsonProcessingException {
        // Should never be called
        throw _illegalCall();
    }

    @Override
    protected void _finish() throws IOException, JsonProcessingException {
        // Should never be called
        throw _illegalCall();
    }
}
