package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * {@link ValueReader} that converts an ISO 8601 string to a {@link ZonedDateTime} instance.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 on Wikipedia</a>
 * @since 2.20
 */
public class OffsetDateTimeValueReader extends DefaultDateTimeValueReader<OffsetDateTime> {
    private final ZoneId _localZoneId;
    
    /**
     * Constructor that accepts a zone ID as a parameter. The zone ID is configured when no zone 
     * or offset was configured in the ISO 8601 string.
     * @param localZoneId Local zone ID
     */
    public OffsetDateTimeValueReader(ZoneId localZoneId) {
        super(OffsetDateTime.class, OffsetDateTime::from);
        _localZoneId = Objects.requireNonNull(localZoneId);
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        final OffsetDateTime odt = (OffsetDateTime)super.read(reader, p);
        
        // If the offset was missing, our formatter defaults to UTC. However, we need to set it 
        // to our preferred local zone to get ISO 8601 compliant behavior. In case our preferred
        // zone already is UTC, no harm is done here.
        if (odt != null && offsetMissing(p.getText())) {
        	return odt.withOffsetSameLocal(_localZoneId.getRules().getOffset(odt.toInstant()));
        }
        return odt;
    }
}
