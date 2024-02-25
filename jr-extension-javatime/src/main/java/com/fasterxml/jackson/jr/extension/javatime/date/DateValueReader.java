package com.fasterxml.jackson.jr.extension.javatime.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValueReader extends ValueReader {
    protected DateValueReader() {
        super(Date.class);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        try {
            return new SimpleDateFormat().parse(p.getText());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
