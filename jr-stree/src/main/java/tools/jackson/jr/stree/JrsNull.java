package tools.jackson.jr.stree;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;

/**
 * Virtual node used instead of `null`, when an operation does not match an
 * actual existing node; this can significantly simplify handling when no
 * null checks are needed.
 */
public final class JrsNull extends JrsValue.Scalar
{
    final static JrsNull instance = new JrsNull();

    public final static JrsNull instance() {
        return instance;
    }

    @Override
    public boolean isNull() {
        return true;
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NULL;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this);
    }

    @Override
    public String toString() {
        // toString() should never return null
        return "null";
    }

    @Override
    public int hashCode() {
        return 3;
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws JacksonException {
        g.writeNull();
    }
}
