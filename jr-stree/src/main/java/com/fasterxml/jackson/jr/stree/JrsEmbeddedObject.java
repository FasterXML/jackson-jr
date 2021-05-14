package com.fasterxml.jackson.jr.stree;

import java.io.IOException;

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
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException {
        if (_value == null) {
            g.writeNull();
        } else if (_value instanceof byte[]) {
            g.writeBinary((byte[]) _value);
        } else {
            g.writeObject(_value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JrsEmbeddedObject that = (JrsEmbeddedObject) o;

        return _value != null ? _value.equals(that._value) : that._value == null;
    }

    @Override
    public int hashCode() {
        return _value != null ? _value.hashCode() : 0;
    }
}
