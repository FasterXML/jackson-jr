package tools.jackson.jr.ob.comp;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.SerializableString;

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

    public ArrayComposer<ObjectComposer<PARENT>> startArrayProperty(String fieldName)
    {
        _closeChild();
        _generator.writeName(fieldName);
        return _startArray(this, _generator);
    }

    public ArrayComposer<ObjectComposer<PARENT>> startArrayProperty(SerializableString fieldName)
    {
        _closeChild();
        _generator.writeName(fieldName);
        return _startArray(this, _generator);
    }
    
    public ObjectComposer<ObjectComposer<PARENT>> startObjectProperty(String fieldName)
    {
        _closeChild();
        _generator.writeName(fieldName);
        return _startObject(this, _generator);
    }

    public ObjectComposer<ObjectComposer<PARENT>> startObjectProperty(SerializableString fieldName)
    {
        _closeChild();
        _generator.writeName(fieldName);
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
        _generator.writeBooleanProperty(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> putNull(String fieldName)
    {
        _generator.writeNullProperty(fieldName);
        return this;
    }

    /**
     * Method used to put a Java Object ("POJO") value into Object being
     * composed:
     * has to be of type that jackson-jr package knows how to serialize.
     */
    public ObjectComposer<PARENT> putObject(String fieldName, Object value)
    {
        _generator.writePOJOProperty(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, int value)
    {
        _generator.writeNumberProperty(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, long value)
    {
        _generator.writeNumberProperty(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, double value)
    {
        _generator.writeNumberProperty(fieldName, value);
        return this;
    }
    
    public ObjectComposer<PARENT> put(String fieldName, String value)
    {
        _generator.writeStringProperty(fieldName, value);
        return this;
    }

    public ObjectComposer<PARENT> put(String fieldName, CharSequence value)
    {
        String str = (value == null) ? null : value.toString();
        _generator.writeStringProperty(fieldName, str);
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
