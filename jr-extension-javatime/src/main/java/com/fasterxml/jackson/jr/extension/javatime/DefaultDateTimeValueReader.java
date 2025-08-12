package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} designed to easily handle {@link TemporalAccessor} descendants such as
 * {@link OffsetDateTime} and {@link ZonedDateTime}. Their string representation is expected to
 * be in ISO 8601 format.
 * @since 2.20
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 */
public class DefaultDateTimeValueReader<T extends TemporalAccessor> extends ValueReader {
    private final TemporalQuery<T> _query;

    /**
     * Constructor that includes a temportal query that is to be used during formatting.
     * @param targetType Target type
     * @param query Temporal query for parsing
     */
    public DefaultDateTimeValueReader(Class<T> targetType, TemporalQuery<T> query) {
        super(targetType);
        
        this._query = Objects.requireNonNull(query);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
    	// SimpleValueReader allows 'Date' objects to be null, so this should probably
    	// also be the case here.
    	if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
    	
        return JavaTimeReaderWriterProvider.FORMATTER.parse(p.getText(), _query);
    }
    
}