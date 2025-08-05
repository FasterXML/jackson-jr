package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

public class DefaultDateTimeValueReader<T extends TemporalAccessor> extends ValueReader {

    private final TemporalQuery<T> _query;

    public DefaultDateTimeValueReader(Class<T> targetType, TemporalQuery<T> query) {
        super(targetType);
        
        this._query = Objects.requireNonNull(query);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        return JavaTimeReaderWriterProvider.FORMATTER.parse(p.getText(), _query);
    }
    
}