package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;

public class ObjectComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;
    
    public ObjectComposer(PARENT parent) {
        super(parent);
        _parent = parent;
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    protected ObjectComposer<PARENT> _start() throws IOException, JsonProcessingException {
        _generator.writeStartObject();
        return this;
    }

    @Override
    protected void _finish() throws IOException, JsonProcessingException
    {
        if (_open) {
            _open = false;
            _generator.writeEndObject();
        }
    }
    
    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public ArrayComposer<ObjectComposer<PARENT>> startArrayField(String fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startArray(this);
    }

    public ArrayComposer<ObjectComposer<PARENT>> startArrayField(SerializableString fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startArray(this);
    }
    
    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(String fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this);
    }

    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(SerializableString fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this);
    }

    public PARENT end()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        if (_open) {
            _open = false;
            _generator.writeEndObject();
            _parent._childClosed();
        }
        return _parent;
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars
    /**********************************************************************
     */

    public ObjectComposer<PARENT> put(String fieldName, boolean value)
        throws IOException, JsonProcessingException
    {
        _generator.writeBooleanField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> putNull(String fieldName)
        throws IOException, JsonProcessingException
    {
        _generator.writeNullField(fieldName);
        return this;
    }
    
    public ObjectComposer<PARENT> put(String fieldName, int value)
        throws IOException, JsonProcessingException
    {
        _generator.writeNumberField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, String value)
        throws IOException, JsonProcessingException
    {
        _generator.writeStringField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, CharSequence value)
        throws IOException, JsonProcessingException
    {
        String str = (value == null) ? null : value.toString();
        _generator.writeStringField(fieldName, str);
        return this;
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected void _closeChild()
        throws IOException, JsonProcessingException
    {
        if (_child != null) {
            _child._finish();
            _child = null;
        }
    }
    
}
