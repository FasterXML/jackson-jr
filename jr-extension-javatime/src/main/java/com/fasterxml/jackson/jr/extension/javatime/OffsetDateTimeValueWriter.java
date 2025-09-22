package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * {@link ValueWriter} that converts a {@link OffsetDateTime} to an ISO 8601 string including
 * an offset.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 * @since 2.20
 */
public class OffsetDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        final String offsetDateTimeString = ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        context.writeValue(offsetDateTimeString);
    }
    
    @Override
    public Class<?> valueType() {
        return OffsetDateTime.class;
    }
}
