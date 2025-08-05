package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

public class OffsetDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        String offsetDateTimeString = ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        context.writeValue(offsetDateTimeString);
    }
    
    @Override
    public Class<?> valueType() {
        return OffsetDateTime.class;
    }
}
