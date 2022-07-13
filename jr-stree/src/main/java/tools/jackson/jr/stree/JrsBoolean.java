package tools.jackson.jr.stree;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;

public class JrsBoolean extends JrsValue.Scalar
{
    public static JrsBoolean TRUE = new JrsBoolean(true, JsonToken.VALUE_TRUE);
    public static JrsBoolean FALSE = new JrsBoolean(false, JsonToken.VALUE_FALSE);

    private final boolean _value;
    private final JsonToken _token;

    private JrsBoolean(boolean v, JsonToken t) {
        _value = v;
        _token = t;
    }

    @Override
    public JsonToken asToken() {
        return _token;
    }

    @Override
    public String asText() {
        return _value ? "true" : "false";
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public boolean booleanValue() {
        return _value;
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws JacksonException {
        g.writeBoolean(_value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JrsBoolean that = (JrsBoolean) o;

        return _value == that._value;
    }

    @Override
    public int hashCode() {
        return (_value ? 1 : 0);
    }
}
