package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ListComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    public ListComposer(PARENT parent) {
        super();
        _parent = parent;
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush()  { }
    
    @Override
    protected ListComposer<PARENT> _start() {
//        _generator.writeStartArray();
        return this;
    }

    @Override
    protected void _finish() {
        if (_open) {
            _open = false;
        }
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public ListComposer<ListComposer<PARENT>> startArray()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        return _startList(this);
    }

    public MapComposer<ListComposer<PARENT>> startObject()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        return _startMap(this);
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public ListComposer<PARENT> add(int value)
        throws IOException, JsonProcessingException
    {
//        _generator.writeNumber(value);
        return this;
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars, textual / binary
    /**********************************************************************
     */

    public ListComposer<PARENT> add(String value)
        throws IOException, JsonProcessingException
    {
//        _generator.writeString(value);
        return this;
    }

    public ListComposer<PARENT> add(CharSequence value)
        throws IOException, JsonProcessingException
    {
        String str = (value == null) ? null : value.toString();
//        _generator.writeString(str);
        return this;
    }

    /*
    /**********************************************************************
    /* Compose methods, scalars, other
    /**********************************************************************
     */

    public ListComposer<PARENT> addNull()
        throws IOException, JsonProcessingException
    {
//        _generator.writeNull();
        return this;
    }

    public ListComposer<PARENT> add(boolean value)
        throws IOException, JsonProcessingException
    {
//        _generator.writeBoolean(value);
        return this;
    }
    
    /**
     * Method used to add Java Object ("POJO") into sequence being
     * composed: this <b>requires</b> that the underlying {@link JsonGenerator}
     * has a properly configure {@link com.fasterxml.jackson.core.ObjectCodec}
     * to use for serializer object.
     */
    public ListComposer<PARENT> addObject(Object pojo)
        throws IOException, JsonProcessingException
    {
//        _generator.writeObject(pojo);
        return this;
    }
    
    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    protected void _closeChild()
    {
        if (_child != null) {
            _child._safeFinish();
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
