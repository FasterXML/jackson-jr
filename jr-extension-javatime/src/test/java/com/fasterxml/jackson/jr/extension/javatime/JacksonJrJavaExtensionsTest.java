package com.fasterxml.jackson.jr.extension.javatime;

import com.fasterxml.jackson.jr.extension.javatime.localdatetime.JacksonJrJavaTimeExtension;
import com.fasterxml.jackson.jr.extension.javatime.date.JacksonJrJavaDateExtension;
import com.fasterxml.jackson.jr.ob.JSON;
import junit.framework.TestCase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

public class JacksonJrJavaExtensionsTest extends TestCase {

    public void testLocalDateTimeTest() throws Exception {
        //Register the extension
        JSON json = JSON.builder().register(new JacksonJrJavaTimeExtension()).build();

        //Test
        LocalDateTime sampleDateTime = LocalDateTime.of(2024, 2, 25, 2, 32);
        String expectedOutput = "{\"aVariable\":\"A_VARIABLE_TEST\",\"dateTime\":\"2024-02-25T02:32:00\"}";
        String aVariableTest = json.asString(new LocalDateTimeClass(sampleDateTime, "A_VARIABLE_TEST"));
        assertEquals(aVariableTest, expectedOutput);
    }

    public void testDateTest() throws Exception {
        //Register the extension
        JSON json = JSON.builder().register(new JacksonJrJavaDateExtension()).build();
        Date date = Date.from(Instant.now());
        String expectedOutput = "{\"aVariable\":\"A_VARIABLE_TEST\",\"date\":\""+ date +"\"}";
        String aVariableTest = json.asString(new DateClass(date, "A_VARIABLE_TEST"));
        assertEquals(aVariableTest, expectedOutput);
    }
}

class DateClass {
    public Date date;
    public String aVariable;

    public DateClass(Date date, String aVariable) {
        this.date = date;
        this.aVariable = aVariable;
    }
}

class LocalDateTimeClass {
    public LocalDateTime dateTime;
    public String aVariable;
    public LocalDateTimeClass(LocalDateTime dateTime, String aVariable) {
        this.dateTime = dateTime;
        this.aVariable = aVariable;
    }
}