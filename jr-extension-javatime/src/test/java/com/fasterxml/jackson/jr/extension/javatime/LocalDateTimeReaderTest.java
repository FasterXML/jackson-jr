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

	private static JSON DATETIME_JSON = JSON.builder().register(new JacksonJrJavaTimeExtension()).build(); 
	
	@Test
	public void testRead() throws JSONObjectException, IOException {
		final LocalDateTime local = DATETIME_JSON.beanFrom(LocalDateTime.class, "\"2025-08-04T14:34:15.123456789\"");
		
		Assertions.assertEquals(2025, local.getYear());
		Assertions.assertEquals(8, local.getMonthValue());
		Assertions.assertEquals(4, local.getDayOfMonth());
		Assertions.assertEquals(14, local.getHour());
		Assertions.assertEquals(34, local.getMinute());
		Assertions.assertEquals(15, local.getSecond());
		Assertions.assertEquals(123, local.getLong(ChronoField.MILLI_OF_SECOND));
		
		final LocalDateTime localWithoutMs = DATETIME_JSON.beanFrom(LocalDateTime.class, "\"2025-08-04T14:34:15\"");
		
		Assertions.assertEquals(2025, localWithoutMs.getYear());
		Assertions.assertEquals(8, localWithoutMs.getMonthValue());
		Assertions.assertEquals(4, localWithoutMs.getDayOfMonth());
		Assertions.assertEquals(14, localWithoutMs.getHour());
		Assertions.assertEquals(34, localWithoutMs.getMinute());
		Assertions.assertEquals(15, localWithoutMs.getSecond());
		Assertions.assertEquals(0, localWithoutMs.getLong(ChronoField.MILLI_OF_SECOND));
		
		final LocalDateTime localWithTz = DATETIME_JSON.beanFrom(LocalDateTime.class, "\"2025-08-04T12:34:15.123Z\"");
		
		Assertions.assertEquals(2025, localWithTz.getYear());
		Assertions.assertEquals(8, localWithTz.getMonthValue());
		Assertions.assertEquals(4, localWithTz.getDayOfMonth());
		Assertions.assertEquals(14, localWithTz.getHour());
		Assertions.assertEquals(34, localWithTz.getMinute());
		Assertions.assertEquals(15, localWithTz.getSecond());
		Assertions.assertEquals(123, localWithTz.getLong(ChronoField.MILLI_OF_SECOND));
		
		final OffsetDateTime offset = DATETIME_JSON.beanFrom(OffsetDateTime.class, "\"2025-08-04T12:34:15.123+02:00\"");
		
		Assertions.assertEquals(2025, offset.getYear());
		Assertions.assertEquals(8, offset.getMonthValue());
		Assertions.assertEquals(4, offset.getDayOfMonth());
		Assertions.assertEquals(12, offset.getHour());
		Assertions.assertEquals(34, offset.getMinute());
		Assertions.assertEquals(15, offset.getSecond());
		Assertions.assertEquals(123, offset.getLong(ChronoField.MILLI_OF_SECOND));
		Assertions.assertEquals(7200, offset.getOffset().getTotalSeconds());
		
		final ZonedDateTime zoned = DATETIME_JSON.beanFrom(ZonedDateTime.class, "\"2025-08-04T12:34:15.123+02:00[Europe/Berlin]\"");
		
		Assertions.assertEquals(2025, zoned.getYear());
		Assertions.assertEquals(8, zoned.getMonthValue());
		Assertions.assertEquals(4, zoned.getDayOfMonth());
		Assertions.assertEquals(12, zoned.getHour());
		Assertions.assertEquals(34, zoned.getMinute());
		Assertions.assertEquals(15, zoned.getSecond());
		Assertions.assertEquals(123, zoned.getLong(ChronoField.MILLI_OF_SECOND));
		Assertions.assertEquals(7200, zoned.getOffset().getTotalSeconds());
		Assertions.assertEquals("Europe/Berlin", zoned.getZone().getId());
	}
	
	@Test
	public void testWrite() throws JSONObjectException, IOException {
		final LocalDateTime ldt = LocalDateTime.of(2025, 8, 4, 14, 34, 15, 123000000);
		final String ldtString = DATETIME_JSON.composeString().addObject(ldt).finish();
		
		Assertions.assertEquals("\"2025-08-04T14:34:15.123\"", ldtString);
		
		final OffsetDateTime odt = OffsetDateTime.of(2025, 8, 4, 12, 34, 15, 123000000, ZoneOffset.ofHours(2));
		final String odtString = DATETIME_JSON.composeString().addObject(odt).finish();
		
		Assertions.assertEquals("\"2025-08-04T12:34:15.123+02:00\"", odtString);
		
		final ZonedDateTime zdt = ZonedDateTime.of(2025, 8, 4, 12, 34, 15, 123000000, ZoneId.of("Europe/Berlin"));
		final String zdtString = DATETIME_JSON.composeString().addObject(zdt).finish();
		
		Assertions.assertEquals("\"2025-08-04T12:34:15.123+02:00[Europe/Berlin]\"", zdtString);
	}
	
}
