package com.fasterxml.jackson.jr.ob.impl;

import java.io.Flushable;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Base class for all composer implementations.
 */
public abstract class ComposerBase implements Flushable
{
    protected final JsonGenerator _generator;

    protected ComposerBase _child;

    protected boolean _open = true;

    protected ComposerBase(JsonGenerator jgen) {
        _generator = jgen;
    }

    protected ComposerBase(ComposerBase parent) {
        _generator = parent._generator;
    }

    /**
     * Calls {@link JsonGenerator#flush} on underlying {@link JsonGenerator}.
     */
    @Override
    public void flush() throws IOException {
        _generator.close();
    }

    /*
    /**********************************************************************
    /* Abstract methods sub-classes have to implement
    /**********************************************************************
     */
    
    protected abstract ComposerBase _start() throws IOException, JsonProcessingException;
    protected abstract void _finish() throws IOException, JsonProcessingException;

    protected final void _childClosed() {
        _child = null;
    }
    
    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */
    
    protected IllegalStateException _illegalCall() {
        return new IllegalStateException("This code path should never be executed");
    }

    protected <P extends ComposerBase> ArrayComposer<P> _startArray(P parent)
        throws IOException, JsonProcessingException
    {
        ArrayComposer<P> child = new ArrayComposer<P>(parent);
        _child = child;
        return child._start();
    }

    protected <P extends ComposerBase> ObjectComposer<P> _startObject(P parent)
            throws IOException, JsonProcessingException
    {
        ObjectComposer<P> child = new ObjectComposer<P>(parent);
        _child = child;
        return child._start();
    }
}
