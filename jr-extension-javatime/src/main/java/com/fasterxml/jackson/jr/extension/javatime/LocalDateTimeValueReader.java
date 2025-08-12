package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} designed specifically to handle {@link LocalDateTime} instances. This 
 * requires a slightly different approach than other date time types because we want to be as 
 * forgiving as possible and be able to also interpret ISO 8601 dates that include an offset 
 * or zone ID. 
 * @since 2.20
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 */
public class LocalDateTimeValueReader extends ValueReader {
    private final ZoneId _localZoneId;
    
    /**
     * Constructor that accepts a zone ID that should be used to when a ISO 8601 string that 
     * includes an offset needs to be converted to a local date time.
     * @param localZoneId Destination zone ID
     */
    public LocalDateTimeValueReader(ZoneId localZoneId) {
        super(LocalDateTime.class);
        _localZoneId = Objects.requireNonNull(localZoneId);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
    	// SimpleValueReader allows 'Date' objects to be null, so this should probably
    	// also be the case here.
    	if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
    	
    	final TemporalAccessor ta = JavaTimeReaderWriterProvider.LOCAL_FORMATTER.parseBest(p.getText(), 
                ZonedDateTime::from, LocalDateTime::from);
        
        if (ta instanceof ZonedDateTime) {
            // Convert a date time that unexpectedly includes a time offset or zone ID, to a proper local date time
            return ((ZonedDateTime)ta).withZoneSameInstant(_localZoneId).toLocalDateTime();
        }
        if (ta instanceof LocalDateTime) {
            return ta;
        }
        
        throw new IOException(String.format("Converting \"%s\" to an instance of %s was "
        		+ "unexpected and should not occur", 
        		p.getText(),
        		ta.getClass().getSimpleName()));
    }
}
