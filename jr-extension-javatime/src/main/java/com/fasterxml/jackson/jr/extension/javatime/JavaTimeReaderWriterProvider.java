package com.fasterxml.jackson.jr.extension.javatime;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * Provider for {@link ValueReader}s and {@link ValueWriter}s for Date/Time
 * types supported by Java Time Extension.
 */
public class JavaTimeReaderWriterProvider extends ReaderWriterProvider
{
    private ZoneId _fallbackLocalZoneId;
    
    protected static final DateTimeFormatter FORMATTER = createFormatter(true);
    protected static final DateTimeFormatter LOCAL_FORMATTER = createFormatter(false);
    
    public JavaTimeReaderWriterProvider() {
        _fallbackLocalZoneId = ZoneId.systemDefault();
    }

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return new LocalDateTimeValueReader(_fallbackLocalZoneId);
        }
        if (OffsetDateTime.class.isAssignableFrom(type)) {
            return new DefaultDateTimeValueReader<OffsetDateTime>(OffsetDateTime.class, OffsetDateTime::from);
        }
        if (ZonedDateTime.class.isAssignableFrom(type)) {
            return new DefaultDateTimeValueReader<ZonedDateTime>(ZonedDateTime.class, ZonedDateTime::from);
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
     * to be converted to a <code>LocalDateTime</code>. Can be set to <code>null</code> to apply
     * the system default.
     * @see java.time.LocalDateTime
     * @param fallbackLocalZoneId Time zone to apply, or <code>null</code>
     * @since 2.20
     * @return Reference for chaining
     */
    public JavaTimeReaderWriterProvider setLocalFallbackTimeZone(ZoneId fallbackLocalZoneId) {
        _fallbackLocalZoneId = fallbackLocalZoneId == null ? ZoneId.systemDefault() : fallbackLocalZoneId;
        return this;
    }
    
    /**
     * Create a forgiving date time formatter that allows different interpretations of ISO 8601
     * strings to be parsed.
     * 
     * @param includeUtcDefault Set to <code>true</code> to set UTC to be the default offset
     *                          for non-local date times. Set to <code>false</code> when handling
     *                          local date times.
 *     @since 2.20
     * @return Formatter
     */
    public static DateTimeFormatter createFormatter(boolean includeUtcDefault) {
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
            builder.parseDefaulting(ChronoField.OFFSET_SECONDS, 0);
        }
        
        return builder.toFormatter();
    }
}
