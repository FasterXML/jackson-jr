package com.fasterxml.jackson.jr.ob.comp;

import java.io.Flushable;
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
    
    protected abstract ComposerBase _start();
    protected abstract Object _finish();

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
    {
        ArrayComposer<P> child = new ArrayComposer<P>(parent, g);
        _child = child;
        return child._start();
    }

    protected <P extends ComposerBase> ObjectComposer<P> _startObject(P parent, JsonGenerator g)
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
