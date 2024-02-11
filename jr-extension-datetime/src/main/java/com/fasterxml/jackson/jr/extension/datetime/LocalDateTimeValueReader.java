package com.fasterxml.jackson.jr.extension.datetime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeValueReader extends ValueReader {
    public LocalDateTimeValueReader() {
        super(LocalDateTime.class);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        return LocalDateTime.parse(p.getText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
