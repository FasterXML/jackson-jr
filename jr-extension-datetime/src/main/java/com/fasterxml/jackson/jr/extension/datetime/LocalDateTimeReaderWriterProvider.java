package com.fasterxml.jackson.jr.extension.datetime;

import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

import java.time.LocalDateTime;

public class LocalDateTimeReaderWriterProvider extends ReaderWriterProvider {
    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        return type.equals(LocalDateTime.class) ? new LocalDateTimeValueReader() : null;
    }

    @Override
    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        return type.equals(LocalDateTime.class) ? new LocalDateTimeValueWriter() : null;
    }
}
