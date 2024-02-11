package com.fasterxml.jackson.jr.extension.datetime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        String localDateTimeString = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        context.writeValue(localDateTimeString);
    }

    @Override
    public Class<?> valueType() {
        return LocalDateTime.class;
    }
}
