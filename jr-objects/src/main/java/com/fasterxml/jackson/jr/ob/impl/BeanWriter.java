package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;

public class BeanWriter
    implements ValueWriter
{
    protected final BeanPropertyWriter[] _properties;

    public BeanWriter(BeanPropertyWriter[] props) {
        _properties = props;
    }
    
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value)
        throws IOException
    {
        context.writeBeanValue(_properties, value);
    }

}
