package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} for {@link java.util.Date}. Reading is lenient: it accepts
 * both numeric timestamps (milliseconds since the Unix epoch) and textual values
 * parsed using the configured {@link DateFormat}.
 *
 * @since 2.23
 */
public class DateValueReader extends ValueReader {
    private final DateFormat _dateFormat;

    public DateValueReader(DateFormat dateFormat) {
        super(Date.class);
        _dateFormat = dateFormat;
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        // Always accept numeric timestamps, regardless of configured format
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return new Date(p.getLongValue());
        }
        final String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            // DateFormat is not thread-safe; readers get a private clone but we
            // still guard against reuse within a single reader instance.
            synchronized (_dateFormat) {
                return _dateFormat.parse(text);
            }
        } catch (ParseException e) {
            throw new JSONObjectException("Failed to parse `java.util.Date` value '"
                    + text + "': " + e.getMessage(), e);
        }
    }
}
