package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

public class ZonedDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        String zonedDateTimeString = ((ZonedDateTime) value).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        context.writeValue(zonedDateTimeString);
    }

    @Override
    public Class<?> valueType() {
        return ZonedDateTime.class;
    }
}
