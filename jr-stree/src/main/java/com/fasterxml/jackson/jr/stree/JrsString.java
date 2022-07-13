package com.fasterxml.jackson.jr.stree;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;

import static tools.jackson.core.JsonToken.VALUE_STRING;

public class JrsString extends JrsValue.Scalar
{
    protected final String _value;

    public JrsString(String v) {
        if (v == null) {
            throw new IllegalArgumentException();
        }
        _value = v;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public JsonToken asToken() {
        return VALUE_STRING;
    }

    @Override
    public String asText() {
        return _value;
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws JacksonException {
        g.writeString(_value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JrsString jrsString = (JrsString) o;

        return _value.equals(jrsString._value);
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }
}
