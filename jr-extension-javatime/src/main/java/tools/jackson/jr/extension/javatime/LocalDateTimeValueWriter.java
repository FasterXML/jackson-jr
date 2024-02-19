package tools.jackson.jr.extension.javatime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.jr.ob.api.ValueWriter;
import tools.jackson.jr.ob.impl.JSONWriter;

public class LocalDateTimeValueWriter implements ValueWriter {
    private final DateTimeFormatter formatter;

    public LocalDateTimeValueWriter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws JacksonException {
        String localDateTimeString = ((LocalDateTime) value).format(formatter);
        context.writeValue(localDateTimeString);
    }

    @Override
    public Class<?> valueType() {
        return LocalDateTime.class;
    }
}
