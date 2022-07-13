package tools.jackson.jr.ob.comp;

import tools.jackson.core.JsonGenerator;

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
    public void flush() {
        if (_generator != null) {
            _generator.close();
        }
    }

    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */

    public ArrayComposer<THIS> startArray()
    {
        _closeChild();
        return _startArray(_this(), _generator);
    }

    public ObjectComposer<THIS> startObject()
    {
        _closeChild();
        return _startObject(_this(), _generator);
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars, number
    /**********************************************************************
     */

    public THIS add(int value)
    {
        _generator.writeNumber(value);
        return _this();
    }

    public THIS add(long value)
    {
        _generator.writeNumber(value);
        return _this();
    }

    public THIS add(double value)
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
    {
        _generator.writeString(value);
        return _this();
    }

    public THIS add(CharSequence value)
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
    {
        _generator.writeNull();
        return _this();
    }

    public THIS add(boolean value)
    {
        _generator.writeBoolean(value);
        return _this();
    }
    
    /**
     * Method used to add Java Object ("POJO") into sequence being
     * composed:
     * has to be of type that jackson-jr package knows how to serialize.
     */
    public THIS addPOJO(Object pojo)
    {
        _generator.writePOJO(pojo);
        return _this();
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    protected void _closeChild()
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
