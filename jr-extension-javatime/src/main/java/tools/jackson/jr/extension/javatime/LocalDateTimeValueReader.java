package tools.jackson.jr.extension.javatime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;

import tools.jackson.jr.ob.api.ValueReader;
import tools.jackson.jr.ob.impl.JSONReader;

public class LocalDateTimeValueReader extends ValueReader {
    private final DateTimeFormatter formatter;

    public LocalDateTimeValueReader(DateTimeFormatter formatter) {
        super(LocalDateTime.class);
        this.formatter = formatter;
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws JacksonException {
        return LocalDateTime.parse(p.getString(), formatter);
    }
}
