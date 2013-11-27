package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class SequenceComposer<THIS extends SequenceComposer<THIS>>
    extends ComposerBase
{
    public SequenceComposer(ComposerBase parent) {
        super(parent);
    }

    protected SequenceComposer(JsonGenerator jgen) {
        super(jgen);
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public ArrayComposer<THIS> startArray()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        return _startArray(_this());
    }

    public ObjectComposer<THIS> startObject()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        return _startObject(_this());
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public THIS add(int value)
        throws IOException, JsonProcessingException
    {
        _generator.writeNumber(value);
        return _this();
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars, textual / binary
    /**********************************************************************
     */

    public THIS add(String value)
        throws IOException, JsonProcessingException
    {
        _generator.writeString(value);
        return _this();
    }

    public THIS add(CharSequence value)
        throws IOException, JsonProcessingException
    {
        String str = (value == null) ? null : value.toString();
        _generator.writeString(str);
        return _this();
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars, other
    /**********************************************************************
     */

    public THIS addNull()
        throws IOException, JsonProcessingException
    {
        _generator.writeNull();
        return _this();
    }

    public THIS add(boolean value)
        throws IOException, JsonProcessingException
    {
        _generator.writeBoolean(value);
        return _this();
    }
    
    /**
     * Method used to add Java Object ("POJO") into sequence being
     * composed: this <b>requires</b> that the underlying {@link JsonGenerator}
     * has a properly configure {@link com.fasterxml.jackson.core.ObjectCodec}
     * to use for serializer object.
     */
    public THIS addObject(Object pojo)
        throws IOException, JsonProcessingException
    {
        _generator.writeObject(pojo);
        return _this();
    }
    
    /*
    /**********************************************************************
    /* Internal helper methods
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

    @SuppressWarnings("unchecked")
    protected THIS _this() {
        return (THIS) this;
    }
}
