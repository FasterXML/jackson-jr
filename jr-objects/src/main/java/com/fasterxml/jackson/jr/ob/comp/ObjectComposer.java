package com.fasterxml.jackson.jr.ob.comp;

import com.fasterxml.jackson.core.JsonGenerator;
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
    public void flush() {
        if (_generator != null) {
            _generator.close();
        }
    }

    @Override
    protected ObjectComposer<PARENT> _start() {
        _generator.writeStartObject();
        return this;
    }

    @Override
    protected Object _finish()
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
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startArray(this, _generator);
    }

    public ArrayComposer<ObjectComposer<PARENT>> startArrayField(SerializableString fieldName)
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startArray(this, _generator);
    }
    
    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(String fieldName)
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this, _generator);
    }

    public ObjectComposer<ObjectComposer<PARENT>> startObjectField(SerializableString fieldName)
    {
        _closeChild();
        _generator.writeFieldName(fieldName);
        return _startObject(this, _generator);
    }

    public PARENT end()
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
    {
        _generator.writeBooleanField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> putNull(String fieldName)
    {
        _generator.writeNullField(fieldName);
        return this;
    }

    /**
     * Method used to put a Java Object ("POJO") value into Object being
     * composed:
     * has to be of type that jackson-jr package knows how to serialize.
     */
    public ObjectComposer<PARENT> putObject(String fieldName, Object value)
    {
        _generator.writeObjectField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, int value)
    {
        _generator.writeNumberField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, long value)
    {
        _generator.writeNumberField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, double value)
    {
        _generator.writeNumberField(fieldName, value);
        return this;
    }
    
    public ObjectComposer<PARENT> put(String fieldName, String value)
    {
        _generator.writeStringField(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, CharSequence value)
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
    {
        if (_child != null) {
            _child._finish();
            _child = null;
        }
    }
}
