package com.fasterxml.jackson.jr.extension.javatime;

import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JavaTimeReaderWriterProvider extends ReaderWriterProvider {
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        return type.equals(LocalDateTime.class) ? new JavaTimeValueReader(formatter) : null;
    }

    @Override
    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        return type.equals(LocalDateTime.class) ? new JavaTimeValueWriter(formatter) : null;
    }

    public JavaTimeReaderWriterProvider withFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
        return this;
    }
}
