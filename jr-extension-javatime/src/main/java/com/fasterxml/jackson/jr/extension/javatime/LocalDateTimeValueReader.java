package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

public class LocalDateTimeValueReader extends ValueReader {
    public LocalDateTimeValueReader() {
        super(LocalDateTime.class);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        final TemporalAccessor ta = JavaTimeReaderWriterProvider.FORMATTER.parseBest(p.getText(), 
                ZonedDateTime::from, LocalDateTime::from);
        
        if (ta instanceof ZonedDateTime) {
            // Convert a date time that unexpectedly includes a time offset or zone ID, to a proper local date time
            return ((ZonedDateTime)ta).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (ta instanceof LocalDateTime) {
            return ta;
        }
        
        throw new IOException("Could not create a valid DateTime instance");
    }
}
