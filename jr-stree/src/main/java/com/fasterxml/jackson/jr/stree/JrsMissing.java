package com.fasterxml.jackson.jr.stree;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Virtual node used instead of `null`, when an operation does not match an
 * actual existing node; this can significantly simplify handling when no
 * null checks are needed.
 */
public final class JrsMissing extends JrsValue
{
    final static JrsMissing instance = new JrsMissing();

    public final static JrsMissing instance() {
        return instance;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.NOT_AVAILABLE;
    }

    @Override
    public boolean isValueNode() {
        return false;
    }

    @Override
    public boolean isContainerNode() {
        return false;
    }

    @Override
    public boolean isMissingNode() {
        return true;
    }

    @Override
    protected JrsValue _at(JsonPointer ptr) {
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public JrsValue get(String s) {
        return null;
    }

    @Override
    public JrsValue get(int i) {
        return null;
    }

    @Override
    public JrsValue path(String s) {
        return this;
    }

    @Override
    public JrsValue path(int i) {
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        return (o == this);
    }

    @Override
    public String toString() {
        // toString() should never return null
        return "";
    }

    @Override
    public int hashCode() {
        return 1;
    }


    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException {
        // not 100% sure what to do... 
        g.writeNull();
    }
}
