package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} for {@link java.util.Calendar}. Like {@link DateValueReader}
 * reading is lenient: it accepts both numeric timestamps (milliseconds since the
 * Unix epoch) and textual values parsed using the configured {@link DateFormat}.
 *
 * @since 2.23
 */
public class CalendarValueReader extends ValueReader {
    private final DateFormat _dateFormat;

    public CalendarValueReader(DateFormat dateFormat) {
        super(Calendar.class);
        _dateFormat = dateFormat;
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        // Always accept numeric timestamps, regardless of configured format
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return _fromMillis(p.getLongValue());
        }
        final String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            final long millis;
            // DateFormat is not thread-safe
            synchronized (_dateFormat) {
                millis = _dateFormat.parse(text).getTime();
            }
            return _fromMillis(millis);
        } catch (ParseException e) {
            throw new JSONObjectException("Failed to parse `java.util.Calendar` value '"
                    + text + "': " + e.getMessage(), e);
        }
    }

    private static Calendar _fromMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c;
    }
}
