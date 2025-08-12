package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * {@link ValueWriter} that converts a {@link LocalDateTime} to an ISO 8601 string without
 * an offset or zone ID.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 */
public class LocalDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        final String localDateTimeString = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        context.writeValue(localDateTimeString);
    }

    @Override
    public Class<?> valueType() {
        return LocalDateTime.class;
    }
}
