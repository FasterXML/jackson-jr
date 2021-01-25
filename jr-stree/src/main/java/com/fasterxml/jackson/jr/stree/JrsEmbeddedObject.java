package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Container for opaque embedded Java objects, exposed by some non-JSON
 * backends: for example, binary formats would expose binary data natively
 * as such tokens.
 */
public class JrsEmbeddedObject extends JrsValue.Scalar
{
    protected final Object _value;

    public JrsEmbeddedObject(Object v) {
        _value = v;
    }

    @Override
    public boolean isEmbeddedValue() {
        return true;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_EMBEDDED_OBJECT;
    }

    @Override
    public String asText() {
        return null;
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public Object embeddedValue() {
        return _value;
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec)
        throws JacksonException
    {
        if (_value == null) {
            g.writeNull();
        } else if (_value instanceof byte[]) {
            g.writeBinary((byte[]) _value);
        } else {
            g.writePOJO(_value);
        }
    }
}
