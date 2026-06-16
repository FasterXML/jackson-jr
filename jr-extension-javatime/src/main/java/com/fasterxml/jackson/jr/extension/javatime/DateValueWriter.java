package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * {@link ValueWriter} for {@link java.util.Date}. By default (when no
 * {@link DateFormat} is configured) values are written as numeric timestamps
 * (milliseconds since the Unix epoch); when a format is supplied they are
 * written as textual values.
 *
 * @since 2.23
 */
public class DateValueWriter implements ValueWriter {
    private final DateFormat _dateFormat;

    public DateValueWriter(DateFormat dateFormat) {
        _dateFormat = dateFormat;
    }

    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
        final Date date = (Date) value;
        if (_dateFormat == null) {
            // Default: numeric timestamp
            g.writeNumber(date.getTime());
        } else {
            final String text;
            // DateFormat is not thread-safe
            synchronized (_dateFormat) {
                text = _dateFormat.format(date);
            }
            g.writeString(text);
        }
    }

    @Override
    public Class<?> valueType() {
        return Date.class;
    }
}
