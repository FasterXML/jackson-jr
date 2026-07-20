package com.fasterxml.jackson.jr.extension.javatime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Tests for reading and writing {@code java.util.Calendar} from/to JSON via the
 * Java Time extension (issue #48). Mirrors {@code java.util.Date} handling:
 * timestamp by default, lenient reads (numeric or textual).
 */
public class JavaUtilCalendarReadWriteTest
{
    // Fixed instant: 2024-02-25T01:52:00.000Z
    private static final long FIXED_MILLIS = 1708825920000L;

    @Test
    public void testCalendarAsTimestampRoundTrip() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final String actual = json.asString(new CalendarClass(calendarOf(FIXED_MILLIS), "A"));
        Assertions.assertEquals("{\"aVariable\":\"A\",\"calendar\":" + FIXED_MILLIS + "}", actual);

        final CalendarClass back = json.beanFrom(CalendarClass.class, actual);
        Assertions.assertEquals(FIXED_MILLIS, back.calendar.getTimeInMillis());
        Assertions.assertEquals("A", back.aVariable);
    }

    @Test
    public void testCalendarAsTextualRoundTrip() throws Exception {
        final JavaTimeReaderWriterProvider provider = new JavaTimeReaderWriterProvider()
                .withDateFormat(utcIsoFormat());
        final JSON json = JSON.builder()
                .register(new JacksonJrJavaTimeExtension().with(provider))
                .build();

        final String actual = json.asString(new CalendarClass(calendarOf(FIXED_MILLIS), "A"));
        Assertions.assertEquals("{\"aVariable\":\"A\",\"calendar\":\"2024-02-25T01:52:00.000Z\"}", actual);

        final CalendarClass back = json.beanFrom(CalendarClass.class, actual);
        Assertions.assertEquals(FIXED_MILLIS, back.calendar.getTimeInMillis());
    }

    @Test
    public void testCalendarReadFromTextualByDefault() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final String input = "{\"aVariable\":\"X\",\"calendar\":\"2024-02-25T01:52:00.000Z\"}";
        final CalendarClass back = json.beanFrom(CalendarClass.class, input);
        Assertions.assertEquals(FIXED_MILLIS, back.calendar.getTimeInMillis());
        Assertions.assertEquals("X", back.aVariable);
    }

    @Test
    public void testCalendarReadFromTimestampWhenTextualConfigured() throws Exception {
        final JavaTimeReaderWriterProvider provider = new JavaTimeReaderWriterProvider()
                .withDateFormat(utcIsoFormat());
        final JSON json = JSON.builder()
                .register(new JacksonJrJavaTimeExtension().with(provider))
                .build();

        final String input = "{\"aVariable\":\"X\",\"calendar\":" + FIXED_MILLIS + "}";
        final CalendarClass back = json.beanFrom(CalendarClass.class, input);
        Assertions.assertEquals(FIXED_MILLIS, back.calendar.getTimeInMillis());
    }

    @Test
    public void testNullCalendar() throws Exception {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        final CalendarClass back = json.beanFrom(CalendarClass.class, "{\"aVariable\":\"X\",\"calendar\":null}");
        Assertions.assertNull(back.calendar);
    }

    private static Calendar calendarOf(long millis) {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(millis);
        return c;
    }

    private static DateFormat utcIsoFormat() {
        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f;
    }

    static class CalendarClass {
        public Calendar calendar;
        public String aVariable;

        public CalendarClass() { }

        public CalendarClass(Calendar calendar, String aVariable) {
            this.calendar = calendar;
            this.aVariable = aVariable;
        }
    }
}
