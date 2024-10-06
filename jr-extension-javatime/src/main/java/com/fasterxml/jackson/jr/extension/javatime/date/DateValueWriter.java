package com.fasterxml.jackson.jr.extension.javatime.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

import java.io.IOException;
import java.util.Date;

public class DateValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        String date = value.toString();
        context.writeValue(date);
    }

    @Override
    public Class<?> valueType() {
        return Date.class;
    }
}
