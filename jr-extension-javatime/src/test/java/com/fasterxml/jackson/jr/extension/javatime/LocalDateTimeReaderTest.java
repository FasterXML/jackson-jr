package com.fasterxml.jackson.jr.extension.javatime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class LocalDateTimeReaderTest {

    @Test
    public void testRead() throws JSONObjectException, IOException {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()
                .with(new JavaTimeReaderWriterProvider().setLocalFallbackTimeZone(ZoneId.of("Europe/Berlin"))))
                .build();
        
        final LocalDateTime local = json.beanFrom(LocalDateTime.class, "\"2025-08-04T14:34:15.123456789\"");
        
        Assertions.assertEquals(2025, local.getYear());
        Assertions.assertEquals(8, local.getMonthValue());
        Assertions.assertEquals(4, local.getDayOfMonth());
        Assertions.assertEquals(14, local.getHour());
        Assertions.assertEquals(34, local.getMinute());
        Assertions.assertEquals(15, local.getSecond());
        Assertions.assertEquals(123, local.getLong(ChronoField.MILLI_OF_SECOND));
        
        final LocalDateTime localWithoutMs = json.beanFrom(LocalDateTime.class, "\"2025-08-04T14:34:15\"");
        
        Assertions.assertEquals(2025, localWithoutMs.getYear());
        Assertions.assertEquals(8, localWithoutMs.getMonthValue());
        Assertions.assertEquals(4, localWithoutMs.getDayOfMonth());
        Assertions.assertEquals(14, localWithoutMs.getHour());
        Assertions.assertEquals(34, localWithoutMs.getMinute());
        Assertions.assertEquals(15, localWithoutMs.getSecond());
        Assertions.assertEquals(0, localWithoutMs.getLong(ChronoField.MILLI_OF_SECOND));
        
        final LocalDateTime localWithTz = json.beanFrom(LocalDateTime.class, "\"2025-08-04T12:34:15.123Z\"");
        
        Assertions.assertEquals(2025, localWithTz.getYear());
        Assertions.assertEquals(8, localWithTz.getMonthValue());
        Assertions.assertEquals(4, localWithTz.getDayOfMonth());
        Assertions.assertEquals(14, localWithTz.getHour());
        Assertions.assertEquals(34, localWithTz.getMinute());
        Assertions.assertEquals(15, localWithTz.getSecond());
        Assertions.assertEquals(123, localWithTz.getLong(ChronoField.MILLI_OF_SECOND));
        
        final LocalDateTime localWithTz2 = json.beanFrom(LocalDateTime.class, "\"2025-08-04T20:34:15.123+08:00\"");
        
        Assertions.assertEquals(2025, localWithTz2.getYear());
        Assertions.assertEquals(8, localWithTz2.getMonthValue());
        Assertions.assertEquals(4, localWithTz2.getDayOfMonth());
        Assertions.assertEquals(14, localWithTz2.getHour());
        Assertions.assertEquals(34, localWithTz2.getMinute());
        Assertions.assertEquals(15, localWithTz2.getSecond());
        Assertions.assertEquals(123, localWithTz2.getLong(ChronoField.MILLI_OF_SECOND));
        
        final OffsetDateTime offsetted = json.beanFrom(OffsetDateTime.class, "\"2025-08-04T12:34:15.123+02:00\"");
        
        Assertions.assertEquals(2025, offsetted.getYear());
        Assertions.assertEquals(8, offsetted.getMonthValue());
        Assertions.assertEquals(4, offsetted.getDayOfMonth());
        Assertions.assertEquals(12, offsetted.getHour());
        Assertions.assertEquals(34, offsetted.getMinute());
        Assertions.assertEquals(15, offsetted.getSecond());
        Assertions.assertEquals(123, offsetted.getLong(ChronoField.MILLI_OF_SECOND));
        Assertions.assertEquals(7200, offsetted.getOffset().getTotalSeconds());
        
        final OffsetDateTime offsettedtWithoutOffset = json.beanFrom(OffsetDateTime.class, "\"2025-08-04T12:34:15.123\"");
        
        Assertions.assertEquals(2025, offsettedtWithoutOffset.getYear());
        Assertions.assertEquals(8, offsettedtWithoutOffset.getMonthValue());
        Assertions.assertEquals(4, offsettedtWithoutOffset.getDayOfMonth());
        Assertions.assertEquals(12, offsettedtWithoutOffset.getHour());
        Assertions.assertEquals(34, offsettedtWithoutOffset.getMinute());
        Assertions.assertEquals(15, offsettedtWithoutOffset.getSecond());
        Assertions.assertEquals(123, offsettedtWithoutOffset.getLong(ChronoField.MILLI_OF_SECOND));
        Assertions.assertEquals(0, offsettedtWithoutOffset.getOffset().getTotalSeconds());
        
        final ZonedDateTime zoned = json.beanFrom(ZonedDateTime.class, "\"2025-08-04T12:34:15.123+02:00[Europe/Berlin]\"");
        
        Assertions.assertEquals(2025, zoned.getYear());
        Assertions.assertEquals(8, zoned.getMonthValue());
        Assertions.assertEquals(4, zoned.getDayOfMonth());
        Assertions.assertEquals(12, zoned.getHour());
        Assertions.assertEquals(34, zoned.getMinute());
        Assertions.assertEquals(15, zoned.getSecond());
        Assertions.assertEquals(123, zoned.getLong(ChronoField.MILLI_OF_SECOND));
        Assertions.assertEquals(7200, zoned.getOffset().getTotalSeconds());
        Assertions.assertEquals("Europe/Berlin", zoned.getZone().getId());
        
        final ZonedDateTime zonedWithoutZoneName = json.beanFrom(ZonedDateTime.class, "\"2025-08-04T12:34:15.123+02:00\"");
        
        Assertions.assertEquals(2025, zonedWithoutZoneName.getYear());
        Assertions.assertEquals(8, zonedWithoutZoneName.getMonthValue());
        Assertions.assertEquals(4, zonedWithoutZoneName.getDayOfMonth());
        Assertions.assertEquals(12, zonedWithoutZoneName.getHour());
        Assertions.assertEquals(34, zonedWithoutZoneName.getMinute());
        Assertions.assertEquals(15, zonedWithoutZoneName.getSecond());
        Assertions.assertEquals(123, zonedWithoutZoneName.getLong(ChronoField.MILLI_OF_SECOND));
        Assertions.assertEquals(7200, zonedWithoutZoneName.getOffset().getTotalSeconds());
        
        final ZonedDateTime zonedWithoutOffset = json.beanFrom(ZonedDateTime.class, "\"2025-08-04T12:34:15.123\"");
        
        Assertions.assertEquals(2025, zonedWithoutOffset.getYear());
        Assertions.assertEquals(8, zonedWithoutOffset.getMonthValue());
        Assertions.assertEquals(4, zonedWithoutOffset.getDayOfMonth());
        Assertions.assertEquals(12, zonedWithoutOffset.getHour());
        Assertions.assertEquals(34, zonedWithoutOffset.getMinute());
        Assertions.assertEquals(15, zonedWithoutOffset.getSecond());
        Assertions.assertEquals(123, zonedWithoutOffset.getLong(ChronoField.MILLI_OF_SECOND));
        Assertions.assertEquals(0, zonedWithoutOffset.getOffset().getTotalSeconds());
    }
    
    @Test
    public void testWrite() throws JSONObjectException, IOException {
        final JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();
        
        final LocalDateTime ldt = LocalDateTime.of(2025, 8, 4, 14, 34, 15, 123000000);
        final String ldtString = json.composeString().addObject(ldt).finish();
        
        Assertions.assertEquals("\"2025-08-04T14:34:15.123\"", ldtString);
        
        final OffsetDateTime odt = OffsetDateTime.of(2025, 8, 4, 12, 34, 15, 123000000, ZoneOffset.ofHours(2));
        final String odtString = json.composeString().addObject(odt).finish();
        
        Assertions.assertEquals("\"2025-08-04T12:34:15.123+02:00\"", odtString);
        
        final ZonedDateTime zdt = ZonedDateTime.of(2025, 8, 4, 12, 34, 15, 123000000, ZoneId.of("Europe/Berlin"));
        final String zdtString = json.composeString().addObject(zdt).finish();
        
        Assertions.assertEquals("\"2025-08-04T12:34:15.123+02:00[Europe/Berlin]\"", zdtString);
    }
    
}
