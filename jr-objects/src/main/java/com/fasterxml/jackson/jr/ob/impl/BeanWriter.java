package com.fasterxml.jackson.jr.ob.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.jr.ob.api.ValueWriter;

public class BeanWriter
    implements ValueWriter
{
    protected final BeanPropertyWriter[] _properties;

    protected final Class<?> _valueType;
    
    public BeanWriter(Class<?> type, BeanPropertyWriter[] props) {
        _valueType = type;
        _properties = props;
    }
    
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value)
        throws JacksonException
    {
        context.writeBeanValue(_properties, value);
    }

    @Override
    public Class<?> valueType() {
        return _valueType;
    }
}
