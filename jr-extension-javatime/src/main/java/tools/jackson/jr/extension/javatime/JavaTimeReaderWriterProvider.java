package tools.jackson.jr.extension.javatime;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import tools.jackson.jr.ob.api.*;
import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;

/**
 * Provider for {@link ValueReader}s and {@link ValueWriter}s for Date/Time
 * types supported by Java Time Extension.
 */
public class JavaTimeReaderWriterProvider extends ReaderWriterProvider
{
    private ZoneId _localZoneId;
    
    protected static final DateTimeFormatter OFFSET_FORMATTER = _createFormatter(true);
    protected static final DateTimeFormatter LOCAL_FORMATTER = _createFormatter(false);
    
    public JavaTimeReaderWriterProvider() {
        withLocalTimeZone(null);
    }

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return new LocalDateTimeValueReader(_localZoneId);
        }
        if (OffsetDateTime.class.isAssignableFrom(type)) {
            return new OffsetDateTimeValueReader(_localZoneId);
        }
        if (ZonedDateTime.class.isAssignableFrom(type)) {
        	return new ZonedDateTimeValueReader(_localZoneId);
        }
        return null;
    }

    @Override
    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return new LocalDateTimeValueWriter();
        }
        if (OffsetDateTime.class.isAssignableFrom(type)) {
            return new OffsetDateTimeValueWriter();
        }
        if (ZonedDateTime.class.isAssignableFrom(type)) {
            return new ZonedDateTimeValueWriter();
        }
        return null;
    }
    
    /**
     * Setter to configure a time zone that is to be applied when a zoned ISO 8601 date time needs
     * to be converted to a {@link LocalDateTime}. Can be set to <code>null</code> to apply the
     * UTC default.
     * @param localZoneId Time zone to apply, or <code>null</code>
     * @since 2.20
     * @return Reference for chaining
     */
    public JavaTimeReaderWriterProvider withLocalTimeZone(ZoneId localZoneId) {
        _localZoneId = localZoneId == null ? ZoneId.of("Z") : localZoneId;
        return this;
    }
    
    /**
     * Convenience method to quickly set the system default time zone as the preferred one. This
     * is equivalent to calling: <code>withLocalTimeZone(ZoneId.systemDefault())</code>.
     * @since 2.20
     * @return Reference for chaining
     */
    public JavaTimeReaderWriterProvider withSystemDefaultTimeZone() {
    	return withLocalTimeZone(ZoneId.systemDefault());
    }
    
    /**
     * Create a forgiving date time formatter that allows different interpretations of ISO 8601
     * strings to be parsed.
     * 
     * @param includeUtcDefault Set to <code>true</code> to set UTC to be the default offset
     *                          for non-local date times that do not have an offset. Set to 
     *                          <code>false</code> when handling local date times.
 *     @since 2.20
     * @return Formatter
     */
    private static DateTimeFormatter _createFormatter(boolean includeUtcDefault) {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .parseLenient()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                    .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .optionalStart()
                    .appendOffsetId()
                    .optionalStart()
                        .appendLiteral('[')
                        .appendZoneRegionId()
                        .appendLiteral(']')
                    .optionalEnd()
                .optionalEnd();
        
        if (includeUtcDefault) {
        	// Without this, parsing a ZonedDateTime or OffsetDateTime will cause an exception if
        	// no offset was specified. So we default the offset to UTC to be safe.
            builder.parseDefaulting(ChronoField.OFFSET_SECONDS, 0);
        }
        
        return builder.toFormatter();
    }
}
