package tools.jackson.jr.extension.javatime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.jr.ob.api.ValueWriter;
import tools.jackson.jr.ob.impl.JSONWriter;

/**
 * {@link ValueWriter} that converts a {@link ZonedDateTime} to an ISO 8601 string including
 * an offset and a zone ID.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 * @since 2.20
 */
public class ZonedDateTimeValueWriter implements ValueWriter {
    @Override
    public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws JacksonException {
        final String zonedDateTimeString = ((ZonedDateTime) value).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        context.writeValue(zonedDateTimeString);
    }

    @Override
    public Class<?> valueType() {
        return ZonedDateTime.class;
    }
}
