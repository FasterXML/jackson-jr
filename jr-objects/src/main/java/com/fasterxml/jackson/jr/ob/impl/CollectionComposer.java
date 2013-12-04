package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.core.JsonGenerator;

public class CollectionComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected Collection<Object> _collection;
    
    public CollectionComposer(PARENT parent) {
        super();
        _parent = parent;
    }

    protected CollectionComposer(Collection<Object> coll) {
        super();
        _parent = null;
        _collection = coll;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static CollectionComposer<?> rootComposer(Collection<Object> coll) {
        return new CollectionComposer(coll);
    }
    
    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush()  { }
    
    @Override
    protected CollectionComposer<PARENT> _start() {
        if (_collection == null) {
            _collection = constructCollection();
        }
        return this;
    }

    @Override
    protected Collection<Object> _finish() {
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

    public CollectionComposer<CollectionComposer<PARENT>> startArray()
    {
        _closeChild();
        return _startCollection(this);
    }

    public MapComposer<CollectionComposer<PARENT>> startObject()
    {
        _closeChild();
        return _startMap(this);
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public CollectionComposer<PARENT> add(int value)
    {
        _collection.add(Integer.valueOf(value));
        return this;
    }

    public CollectionComposer<PARENT> add(long value)
    {
        _collection.add(Long.valueOf(value));
        return this;
    }

    public CollectionComposer<PARENT> add(double value)
    {
        _collection.add(Double.valueOf(value));
        return this;
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, textual / binary
    /**********************************************************************
     */

    public CollectionComposer<PARENT> add(String value)
    {
        _collection.add(value);
        return this;
    }

    public CollectionComposer<PARENT> add(CharSequence value)
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

    public CollectionComposer<PARENT> addNull()
    {
        _collection.add(null);
        return this;
    }

    public CollectionComposer<PARENT> add(boolean value)
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
    public CollectionComposer<PARENT> addObject(Object pojo)
    {
        _collection.add(pojo);
        return this;
    }
    
    /*
    /**********************************************************************
    /* Overridable helper methods
    /**********************************************************************
     */
    
    protected Collection<Object> constructCollection() {
        return new ArrayList<Object>();
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
