package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;

public class JrsNumber extends JrsValue.Scalar
{
    private static final Map<Class<? extends Number>, JsonParser.NumberType> NUMBER_TYPES;
    static
    {
        final Map<Class<? extends Number>, JsonParser.NumberType> numberTypes = new HashMap<Class<? extends Number>, JsonParser.NumberType>();

        numberTypes.put(Byte.class, JsonParser.NumberType.INT);
        numberTypes.put(Short.class, JsonParser.NumberType.INT);
        numberTypes.put(Integer.class, JsonParser.NumberType.INT);
        numberTypes.put(Long.class, JsonParser.NumberType.LONG);
        numberTypes.put(BigInteger.class, JsonParser.NumberType.BIG_INTEGER);
        numberTypes.put(Float.class, JsonParser.NumberType.FLOAT);
        numberTypes.put(Double.class, JsonParser.NumberType.DOUBLE);
        numberTypes.put(BigDecimal.class, JsonParser.NumberType.BIG_DECIMAL);

        NUMBER_TYPES = Collections.unmodifiableMap(numberTypes);
    }

    private final Number _value;
    private final JsonParser.NumberType _numberType;

    public JrsNumber(Number value)
    {
        _value = value;
        _numberType = NUMBER_TYPES.get(value.getClass());
        if (_numberType == null) {
            throw new IllegalArgumentException("Unsupported Number type: "+value.getClass().getName());
        }
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    public Number getValue() {
        return _value;
    }

    @Override
    public JsonToken asToken() {
        switch (numberType())
        {
            case BIG_DECIMAL:
            case DOUBLE:
            case FLOAT:
                return VALUE_NUMBER_FLOAT;
            default:
                return VALUE_NUMBER_INT;
        }
    }

    @Override
    public String asText() {
        return String.valueOf(_value);
    }

    @Override
    public JsonParser.NumberType numberType() {
        return _numberType;
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public BigInteger asBigInteger() throws IOException {
        if (_value instanceof BigInteger) {
            return (BigInteger) _value;
        }
        if (_value instanceof BigDecimal) {
            BigDecimal dec = (BigDecimal) _value;
            return dec.toBigInteger();
        }
        return BigInteger.valueOf(_value.longValue());
    }

    public BigDecimal asBigDecimal() throws IOException {
        if (_value instanceof BigDecimal) {
            return (BigDecimal) _value;
        }
        if (_value instanceof BigInteger) {
            return new BigDecimal((BigInteger) _value);
        }
        if ((_value instanceof Double) || (_value instanceof Float)) {
            return new BigDecimal(_value.doubleValue());
        }
        return new BigDecimal(_value.longValue());
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException
    {
        switch (numberType()) {
        case INT:
            g.writeNumber(_value.intValue());
            break;
        case LONG:
            g.writeNumber(_value.longValue());
            break;
        case BIG_INTEGER:
            g.writeNumber((BigInteger) _value);
            break;
        case FLOAT:
            g.writeNumber(_value.floatValue());
            break;
        case BIG_DECIMAL:
            g.writeNumber((BigDecimal) _value);
            break;
        case DOUBLE:
        default:
            g.writeNumber(_value.doubleValue());
            break;
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

        JrsNumber jrsNumber = (JrsNumber) o;

        if (_value != null ? !_value.equals(jrsNumber._value) : jrsNumber._value != null) {
            return false;
        }
        return _numberType == jrsNumber._numberType;
    }

    @Override
    public int hashCode() {
        int result = _value != null ? _value.hashCode() : 0;
        result = 31 * result + (_numberType != null ? _numberType.hashCode() : 0);
        return result;
    }
}
