package tools.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.jr.ob.api.ValueReader;
import tools.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} that converts an ISO 8601 string to a {@link ZonedDateTime} instance.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 * @since 2.20
 */
public class ZonedDateTimeValueReader extends DefaultDateTimeValueReader<ZonedDateTime> {
    private final ZoneId _localZoneId;
    
    /**
     * Constructor that accepts a zone ID as a parameter. The zone ID is configured when no zone 
     * or offset was configured in the ISO 8601 string.
     * @param localZoneId Local zone ID
     */
    public ZonedDateTimeValueReader(ZoneId localZoneId) {
        super(ZonedDateTime.class, ZonedDateTime::from);
        _localZoneId = Objects.requireNonNull(localZoneId);
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws JacksonException {
        final ZonedDateTime zdt = (ZonedDateTime)super.read(reader, p);
        
        // If the offset was missing, our formatter defaults to UTC. However, we need to set it 
        // to our preferred local zone to get ISO 8601 compliant behavior. In case our preferred
        // zone already is UTC, no harm is done here.
        if (zdt != null && offsetMissing(p.getString())) {
        	return zdt.withZoneSameLocal(_localZoneId);
        }
        return zdt;
    }
}
