package tools.jackson.jr.ob.api;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.jr.ob.impl.JSONWriter;

public interface ValueWriter
{
    public void writeValue(JSONWriter context, JsonGenerator g, Object value)
        throws JacksonException;

    /*
    /**********************************************************************
    /* Minimal metadata
    /**********************************************************************
     */

    /**
     * Accessor for non-generic (type-erased) type of values this reader
     * produces from input.
     */
    public Class<?> valueType();
}
