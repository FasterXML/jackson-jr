package tools.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.ValueReader;
import tools.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} designed specifically to handle {@link LocalDateTime} instances. This 
 * requires a slightly different approach than other date time types because we want to be as 
 * forgiving as possible and be able to also interpret ISO 8601 dates that include an offset 
 * or zone ID. 
 * @since 2.20
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 */
public class LocalDateTimeValueReader extends ValueReader {
    private final ZoneId _localZoneId;
    
    /**
     * Constructor that accepts a zone ID that should be used to when a ISO 8601 string that 
     * includes an offset needs to be converted to a local date time.
     * @param localZoneId Destination zone ID
     */
    public LocalDateTimeValueReader(ZoneId localZoneId) {
        super(LocalDateTime.class);
        _localZoneId = Objects.requireNonNull(localZoneId);
    }

    @Override
    public Object read(JSONReader reader, JsonParser p) throws JacksonException {
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            final TemporalAccessor ta = JavaTimeReaderWriterProvider.LOCAL_FORMATTER.parseBest(p.getString(), 
                    ZonedDateTime::from, LocalDateTime::from);
            
            if (ta instanceof ZonedDateTime) {
                // Convert a date time that unexpectedly includes a time offset or zone ID, to a 
                // local date time
                return ((ZonedDateTime)ta).withZoneSameInstant(_localZoneId).toLocalDateTime();
            }
            if (ta instanceof LocalDateTime) {
                return ta;
            }
        }
     
        throw JSONObjectException.from(p,
                "Can not create a "+_valueType.getName()+" instance out of "+_tokenDesc(p));
    }
}
