package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

public class LocalDateTimeValueReader extends ValueReader {
    private final ZoneId _localZoneId;
    
    public LocalDateTimeValueReader(ZoneId localZoneId) {
        super(LocalDateTime.class);
        _localZoneId = Objects.requireNonNull(localZoneId);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        final TemporalAccessor ta = JavaTimeReaderWriterProvider.LOCAL_FORMATTER.parseBest(p.getText(), 
                ZonedDateTime::from, LocalDateTime::from);
        
        if (ta instanceof ZonedDateTime) {
            // Convert a date time that unexpectedly includes a time offset or zone ID, to a proper local date time
            return ((ZonedDateTime)ta).withZoneSameInstant(_localZoneId).toLocalDateTime();
        }
        if (ta instanceof LocalDateTime) {
            return ta;
        }
        
        throw new IOException("Could not create a valid DateTime instance");
    }
}
