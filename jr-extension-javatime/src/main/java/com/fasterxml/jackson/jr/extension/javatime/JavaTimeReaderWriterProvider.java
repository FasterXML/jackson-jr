package com.fasterxml.jackson.jr.extension.javatime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
    /**
     * ISO-8601 pattern used as the default for parsing textual
     * {@link java.util.Date} / {@link java.util.Calendar} values (and for
     * writing them when textual serialization is enabled via
     * {@link #withDateFormat}).
     *
     * @since 2.23
     */
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private ZoneId _localZoneId;

    /**
     * Optional format for {@link java.util.Date} and {@link java.util.Calendar}
     * serialization. When left {@code null}, values are written as numeric
     * timestamps (milliseconds since the Unix epoch); reading always accepts
     * both numeric timestamps and textual values.
     *
     * @since 2.23
     */
    private DateFormat _dateFormat = null;

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
        if (Date.class.isAssignableFrom(type)) {
            // Always supply a format so textual values can be read, even when
            // serialization defaults to numeric timestamps.
            return new DateValueReader(_dateFormatForReading());
        }
        if (Calendar.class.isAssignableFrom(type)) {
            return new CalendarValueReader(_dateFormatForReading());
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
        if (Date.class.isAssignableFrom(type)) {
            // null format => numeric timestamp (default)
            return new DateValueWriter(_dateFormatForWriting());
        }
        if (Calendar.class.isAssignableFrom(type)) {
            return new CalendarValueWriter(_dateFormatForWriting());
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
     * Method for configuring the {@link DateFormat} used for textual
     * serialization and deserialization of {@link java.util.Date} and
     * {@link java.util.Calendar} values.
     *<p>
     * When set, those values are <i>written</i> as JSON Strings using this
     * format (instead of the default numeric timestamp), and textual values are
     * <i>read</i> using this same format. Numeric timestamps are always accepted
     * on read regardless of this setting.
     *
     * @param df {@link DateFormat} instance, or {@code null} to restore the
     *   default (numeric timestamp serialization)
     * @since 2.23
     * @return This provider instance for call chaining
     */
    public JavaTimeReaderWriterProvider withDateFormat(DateFormat df) {
        _dateFormat = df;
        return this;
    }

    /**
     * @return Private clone of the configured format, or {@code null} when none
     *   is configured (meaning: write numeric timestamps).
     */
    private DateFormat _dateFormatForWriting() {
        return (_dateFormat == null) ? null : (DateFormat) _dateFormat.clone();
    }

    /**
     * @return A format usable for reading textual values; falls back to a
     *   default ISO-8601 (UTC) format when none is configured, so textual input
     *   is always accepted.
     */
    private DateFormat _dateFormatForReading() {
        if (_dateFormat != null) {
            return (DateFormat) _dateFormat.clone();
        }
        SimpleDateFormat f = new SimpleDateFormat(DEFAULT_DATE_PATTERN, Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f;
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
