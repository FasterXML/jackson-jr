package com.fasterxml.jackson.jr.ob.comp;

import tools.jackson.core.JsonGenerator;

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
    protected ArrayComposer<PARENT> _start() {
        _generator.writeStartArray();
        return this;
    }

    @Override
    protected Object _finish() {
        if (_open) {
            _open = false;
            _generator.writeEndArray();
        }
        return null;
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
            _generator.writeEndArray();
            _parent._childClosed();
        }
        return _parent;
    }
}
