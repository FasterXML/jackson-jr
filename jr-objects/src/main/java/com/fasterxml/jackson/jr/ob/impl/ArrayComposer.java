package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ArrayComposer<PARENT extends ComposerBase>
    extends SequenceComposer<ArrayComposer<PARENT>>
{
    protected final PARENT _parent;

    public ArrayComposer(PARENT parent, JsonGenerator g) {
        super(g);
        _parent = parent;
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    protected ArrayComposer<PARENT> _start() throws IOException, JsonProcessingException {
        _generator.writeStartArray();
        return this;
    }

    @Override
    protected void _finish() throws IOException, JsonProcessingException {
        if (_open) {
            _open = false;
            _generator.writeEndArray();
        }
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */
    
    public PARENT end()
        throws IOException, JsonProcessingException
    {
        _closeChild();
        if (_open) {
            _open = false;
            _generator.writeEndArray();
            _parent._childClosed();
        }
        return _parent;
    }
}
