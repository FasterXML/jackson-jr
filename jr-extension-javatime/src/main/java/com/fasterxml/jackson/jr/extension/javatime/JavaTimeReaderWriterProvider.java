package com.fasterxml.jackson.jr.extension.javatime;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    protected static final DateTimeFormatter FORMATTER;
        
    static {
        FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                    .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .optionalStart()
                    .appendOffsetId()
                .optionalEnd()
                .optionalStart()
                    .appendLiteral('[')
                    .appendZoneRegionId()
                    .appendLiteral(']')
                .optionalEnd()
            .toFormatter();
    }
    
    public JavaTimeReaderWriterProvider() { }

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return new LocalDateTimeValueReader();
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
}
