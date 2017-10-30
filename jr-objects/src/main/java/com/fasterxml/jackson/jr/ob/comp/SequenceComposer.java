package com.fasterxml.jackson.jr.ob.comp;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public abstract class SequenceComposer<THIS extends SequenceComposer<THIS>>
    extends ComposerBase
{
    protected final JsonGenerator _generator;
    
    public SequenceComposer(JsonGenerator g) {
        super();
        _generator = g;
    }

    /*
    /**********************************************************************
    /* Abstract methods from base class
    /**********************************************************************
     */
    
    /**
     * Calls {@link JsonGenerator#flush} on underlying {@link JsonGenerator}.
     */
    @Override
    public void flush() throws IOException {
        if (_generator != null) {
            _generator.close();
        }
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public ArrayComposer<THIS> startArray() throws IOException
    {
        _closeChild();
        return _startArray(_this(), _generator);
    }

    public ObjectComposer<THIS> startObject() throws IOException
    {
        _closeChild();
        return _startObject(_this(), _generator);
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public THIS add(int value) throws IOException
    {
        _generator.writeNumber(value);
        return _this();
    }

    public THIS add(long value) throws IOException
    {
        _generator.writeNumber(value);
        return _this();
    }

    public THIS add(double value) throws IOException
    {
        _generator.writeNumber(value);
        return _this();
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, textual / binary
    /**********************************************************************
     */

    public THIS add(String value) throws IOException
    {
        _generator.writeString(value);
        return _this();
    }

    public THIS add(CharSequence value) throws IOException
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

    public THIS addNull() throws IOException
    {
        _generator.writeNull();
        return _this();
    }

    public THIS add(boolean value) throws IOException
    {
        _generator.writeBoolean(value);
        return _this();
    }
    
    /**
     * Method used to add Java Object ("POJO") into sequence being
     * composed:
     * has to be of type that jackson-jr package knows how to serialize.
     */
    public THIS addObject(Object pojo) throws IOException
    {
        _generator.writeObject(pojo);
        return _this();
    }
    
    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    protected void _closeChild() throws IOException
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
