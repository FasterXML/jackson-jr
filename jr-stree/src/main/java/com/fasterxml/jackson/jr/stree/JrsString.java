package com.fasterxml.jackson.jr.stree;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

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
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException {
        g.writeString(_value);
    }
}
