package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.SerializableString;

public class MapComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected String _fieldName;

    protected Map<String,Object> _map;
    
    public MapComposer(PARENT parent) {
        super();
        _parent = parent;
    }

    public MapComposer(Map<String,Object> map) {
        super();
        _parent = null;
        _map = map;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static MapComposer<?> rootComposer(Map<String,Object> map) {
        return new MapComposer(map);
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush() throws IOException { }

    @Override
    protected MapComposer<PARENT> _start() {
        if (_map == null) {
            _map = constructMap();
        }
        return this;
    }
    
    @Override
    protected Map<String,Object> _finish()
    {
        if (_open) {
            _open = false;
        }
        return _map;
    }
    
    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */
    
    public CollectionComposer<MapComposer<PARENT>> startArrayField(String fieldName)
    {
        _closeChild();
        _fieldName = fieldName;
        return _startCollection(this);
    }
    
    public CollectionComposer<MapComposer<PARENT>> startArrayField(SerializableString fieldName)
    {
        _closeChild();
        _fieldName = fieldName.getValue();
        return _startCollection(this);
    }
    
    public MapComposer<MapComposer<PARENT>> startObjectField(String fieldName)
    {
        _closeChild();
        _fieldName = fieldName;
        return _startMap(this);
    }
    
    public MapComposer<MapComposer<PARENT>> startObjectField(SerializableString fieldName)
    {
        _closeChild();
        _fieldName = fieldName.getValue();
        return _startMap(this);
    }
    
    public PARENT end()
    {
        _closeChild();
        if (_open) {
            _open = false;
            _parent._childClosed();
        }
        return _parent;
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars
    /**********************************************************************
     */
    
    public MapComposer<PARENT> put(String fieldName, boolean value)
    {
        _map.put(fieldName, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }
    
    public MapComposer<PARENT> putNull(String fieldName)
    {
        // could maybe just omit but...
        _map.put(fieldName, null);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, int value)
    {
        _map.put(fieldName, value);
        return this;
    }

    public MapComposer<PARENT> put(String fieldName, long value)
    {
        _map.put(fieldName, value);
        return this;
    }

    public MapComposer<PARENT> put(String fieldName, double value)
    {
        _map.put(fieldName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, String value)
    {
        _map.put(fieldName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, CharSequence value)
    {
        String str = (value == null) ? null : value.toString();
        _map.put(fieldName, str);
        return this;
    }

    /*
    /**********************************************************************
    /* Overridable helper methods
    /**********************************************************************
     */
    
    protected Map<String,Object> constructMap() {
        return new LinkedHashMap<String,Object>();
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected void _closeChild()
    {
        if (_child != null) {
            Object value = _child._safeFinish();
            _map.put(_fieldName, value);
            _child = null;
        }
    }
}
