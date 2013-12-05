package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.core.JsonGenerator;

public class CollectionComposer<PARENT extends ComposerBase,
    C extends Collection<Object>>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected C _collection;
    
    public CollectionComposer(PARENT parent) {
        super();
        _parent = parent;
    }

    public CollectionComposer(C coll) {
        super();
        _parent = null;
        _collection = coll;
    }

    public static <T extends Collection<Object>> CollectionComposer<?,T>
    rootComposer(T coll) {
        return new CollectionComposer<ComposerBase,T>(coll);
    }
    
    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush()  { }
    
    @Override
    protected CollectionComposer<PARENT,C> _start() {
        if (_collection == null) {
            _collection = constructCollection();
        }
        return this;
    }

    @Override
    protected C _finish() {
        if (_open) {
            _open = false;
        }
        return _collection;
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public CollectionComposer<CollectionComposer<PARENT,C>,?> startArray()
    {
        _closeChild();
        return _startCollection(this);
    }

    public MapComposer<CollectionComposer<PARENT,C>> startObject()
    {
        _closeChild();
        return _startMap(this);
    }

    public C finish() {
        return _finish();
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public CollectionComposer<PARENT,C> add(int value)
    {
        _collection.add(Integer.valueOf(value));
        return this;
    }

    public CollectionComposer<PARENT,C> add(long value)
    {
        _collection.add(Long.valueOf(value));
        return this;
    }

    public CollectionComposer<PARENT,C> add(double value)
    {
        _collection.add(Double.valueOf(value));
        return this;
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, textual / binary
    /**********************************************************************
     */

    public CollectionComposer<PARENT,C> add(String value)
    {
        _collection.add(value);
        return this;
    }

    public CollectionComposer<PARENT,C> add(CharSequence value)
    {
        String str = (value == null) ? null : value.toString();
        _collection.add(str);
        return this;
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars, other
    /**********************************************************************
     */

    public CollectionComposer<PARENT,C> addNull()
    {
        _collection.add(null);
        return this;
    }

    public CollectionComposer<PARENT,C> add(boolean value)
    {
        _collection.add(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }
    
    /**
     * Method used to add Java Object ("POJO") into sequence being
     * composed: this <b>requires</b> that the underlying {@link JsonGenerator}
     * has a properly configure {@link com.fasterxml.jackson.core.ObjectCodec}
     * to use for serializer object.
     */
    public CollectionComposer<PARENT,C> addObject(Object pojo)
    {
        _collection.add(pojo);
        return this;
    }
    
    /*
    /**********************************************************************
    /* Overridable helper methods
    /**********************************************************************
     */
    
    @SuppressWarnings("unchecked")
    protected C constructCollection() {
        return (C) new ArrayList<Object>();
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    protected void _closeChild()
    {
        if (_child != null) {
            Object value = _child._safeFinish();
            _collection.add(value);
            _child = null;
        }
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */
    
    public PARENT end()
    {
        _closeChild();
        if (_open) {
            _open = false;
            _parent._childClosed();
        }
        return _parent;
    }
}
