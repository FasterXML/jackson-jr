package com.fasterxml.jackson.jr.ob.comp;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;

public class ObjectComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected final JsonGenerator _generator;
    
    public ObjectComposer(PARENT parent, JsonGenerator g) {
        super();
        _parent = parent;
        _generator = g;
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush() throws IOException {
        if (_generator != null) {
            _generator.close();
        }
    }

    @Override
    protected ObjectComposer<PARENT> _start() throws IOException, JsonProcessingException {
        _generator.writeStartObject();
        return this;
    }

    @Override
    protected Object _finish() throws IOException, JsonProcessingException
    {
        if (_open) {
            _open = false;
            _generator.writeEndObject();
        }
        return null;
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
        return _startArray(this, _generator);
    }

    public ArrayComposer<ObjectComposer<PARENT>> startArrayField(SerializableString fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startArray(this, _generator);
    }
    
    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(String fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this, _generator);
    }

    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(SerializableString fieldName)
        throws IOException, JsonProcessingException
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this, _generator);
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

    public ObjectComposer<PARENT> put(String fieldName, long value)
        throws IOException, JsonProcessingException
    {
        _generator.writeNumberField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, double value)
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
