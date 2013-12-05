package com.fasterxml.jackson.jr.ob.impl;

import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Base class for all composer implementations.
 */
public abstract class ComposerBase
    implements Flushable
{
    protected ComposerBase _child;

    protected boolean _open = true;

    protected ComposerBase() { }

    /*
    /**********************************************************************
    /* Abstract methods sub-classes have to implement
    /**********************************************************************
     */
    
    protected abstract ComposerBase _start() throws IOException;
    protected abstract Object _finish() throws IOException;

    /**
     * Helper method used to "peel away" bogus exception declaration
     */
    protected Object _safeFinish() {
        try {
            return _finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
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

    protected <P extends ComposerBase> ArrayComposer<P> _startArray(P parent, JsonGenerator g)
        throws IOException
    {
        ArrayComposer<P> child = new ArrayComposer<P>(parent, g);
        _child = child;
        return child._start();
    }

    protected <P extends ComposerBase> ObjectComposer<P> _startObject(P parent, JsonGenerator g)
            throws IOException
    {
        ObjectComposer<P> child = new ObjectComposer<P>(parent, g);
        _child = child;
        return child._start();
    }

    protected <P extends ComposerBase> CollectionComposer<P,?> _startCollection(P parent)
    {
        CollectionComposer<P,?> child = new CollectionComposer<P,Collection<Object>>(parent);
        _child = child;
        return child._start();
    }

    protected <P extends ComposerBase> MapComposer<P> _startMap(P parent)
    {
        MapComposer<P> child = new MapComposer<P>(parent);
        _child = child;
        return child._start();
    }
}
