package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.SerializableString;

public class MapComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected String _fieldName;
    
    public MapComposer(PARENT parent) {
        super();
        _parent = parent;
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
        return this;
    }
    
    @Override
    protected void _finish()
    {
        if (_open) {
            _open = false;
        }
    }
    
    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */
    
    public ListComposer<MapComposer<PARENT>> startArrayField(String fieldName)
    {
        _closeChild();
        _fieldName = fieldName;
        return _startList(this);
    }
    
    public ListComposer<MapComposer<PARENT>> startArrayField(SerializableString fieldName)
    {
        _closeChild();
        _fieldName = fieldName.getValue();
        return _startList(this);
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
//        _generator.writeBooleanField(fieldName, value);
        return this;
    }
    
    public MapComposer<PARENT> putNull(String fieldName)
    {
//        _generator.writeNullField(fieldName);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, int value)
    {
//        _generator.writeNumberField(fieldName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, String value)
    {
//        _generator.writeStringField(fieldName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String fieldName, CharSequence value)
    {
        String str = (value == null) ? null : value.toString();
//        _generator.writeStringField(fieldName, str);
        return this;
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected void _closeChild()
    {
        if (_child != null) {
            _child._safeFinish();
            _child = null;
        }
    }
}
