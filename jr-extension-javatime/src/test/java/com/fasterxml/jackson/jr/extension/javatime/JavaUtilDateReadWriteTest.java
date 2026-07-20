package com.fasterxml.jackson.jr.extension.javatime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Tests for reading and writing {@code java.util.Date} from/to JSON via the
 * Java Time extension (issue #48). By default dates are written as numeric
 * timestamps; reading accepts both numeric timestamps and textual values.
 */
public class JavaUtilDateReadWriteTest
{
    // Fixed instant: 2024-02-25T01:52:00.000Z
    private static final long FIXED_MILLIS = 1708825920000L;

    /**
     * By default {@code java.util.Date} is written as a numeric timestamp and
     * read back from it (issue #48: read what we write).
     */
    @Test
    public void testDateAsTimestampRoundTrip() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final Date date = new Date(FIXED_MILLIS);
        final String actual = json.asString(new DateClass(date, "A_VARIABLE_TEST"));
        Assertions.assertEquals("{\"aVariable\":\"A_VARIABLE_TEST\",\"date\":" + FIXED_MILLIS + "}", actual);

        final DateClass back = json.beanFrom(DateClass.class, actual);
        Assertions.assertEquals(date, back.date);
        Assertions.assertEquals("A_VARIABLE_TEST", back.aVariable);
    }

    /**
     * When a {@link DateFormat} is configured, dates are written as textual
     * values and read back using the same format.
     */
    @Test
    public void testDateAsTextualRoundTrip() throws Exception {
        final JavaTimeReaderWriterProvider provider = new JavaTimeReaderWriterProvider()
                .withDateFormat(utcIsoFormat());
        final JSON json = JSON.builder()
                .register(new JacksonJrJavaTimeExtension().with(provider))
                .build();

        final Date date = new Date(FIXED_MILLIS);
        final String actual = json.asString(new DateClass(date, "A_VARIABLE_TEST"));
        Assertions.assertEquals("{\"aVariable\":\"A_VARIABLE_TEST\",\"date\":\"2024-02-25T01:52:00.000Z\"}", actual);

        final DateClass back = json.beanFrom(DateClass.class, actual);
        Assertions.assertEquals(date, back.date);
    }

    /**
     * Reading is lenient: even with default (timestamp) configuration a textual
     * value is accepted, using the default ISO-8601 format.
     */
    @Test
    public void testDateReadFromTextualByDefault() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final String input = "{\"aVariable\":\"X\",\"date\":\"2024-02-25T01:52:00.000Z\"}";
        final DateClass back = json.beanFrom(DateClass.class, input);
        Assertions.assertEquals(new Date(FIXED_MILLIS), back.date);
        Assertions.assertEquals("X", back.aVariable);
    }

    /**
     * Conversely, with a textual format configured a numeric timestamp is still
     * accepted on read.
     */
    @Test
    public void testDateReadFromTimestampWhenTextualConfigured() throws Exception {
        final JavaTimeReaderWriterProvider provider = new JavaTimeReaderWriterProvider()
                .withDateFormat(utcIsoFormat());
        final JSON json = JSON.builder()
                .register(new JacksonJrJavaTimeExtension().with(provider))
                .build();

        final String input = "{\"aVariable\":\"X\",\"date\":" + FIXED_MILLIS + "}";
        final DateClass back = json.beanFrom(DateClass.class, input);
        Assertions.assertEquals(new Date(FIXED_MILLIS), back.date);
    }

    @Test
    public void testNullDate() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final DateClass back = json.beanFrom(DateClass.class, "{\"aVariable\":\"X\",\"date\":null}");
        Assertions.assertNull(back.date);
    }

    private static DateFormat utcIsoFormat() {
        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f;
    }

    static class DateClass {
        public Date date;
        public String aVariable;

        public DateClass() { }

        public DateClass(Date date, String aVariable) {
            this.date = date;
            this.aVariable = aVariable;
        }
    }
}
