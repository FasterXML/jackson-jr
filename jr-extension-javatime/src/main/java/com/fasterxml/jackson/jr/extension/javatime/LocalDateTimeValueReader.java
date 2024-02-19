package com.fasterxml.jackson.jr.extension.javatime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeValueReader extends ValueReader {
    private final DateTimeFormatter formatter;

    public LocalDateTimeValueReader(DateTimeFormatter formatter) {
        super(LocalDateTime.class);
        this.formatter = formatter;
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        return LocalDateTime.parse(p.getText(), formatter);
    }
}
