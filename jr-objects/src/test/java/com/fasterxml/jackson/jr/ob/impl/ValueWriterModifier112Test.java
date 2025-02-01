package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueWriterModifier112Test extends TestBase
{
    static class TestBean112 {
        public Path p1;
        public Path p2;
    }

    static class StringBean112 {
        public String s1;
        public String s2;
        public String s3;
    }

    static class PathWriter implements ValueWriter {
        @Override
        public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
            g.writeString(((Path) value).toString().replace(File.separatorChar, '/'));
        }

        @Override
        public Class<?> valueType() {
            return Path.class;
        }
    }

    static class UpperCaseWriter implements ValueWriter {
        @Override
        public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
            g.writeString(String.valueOf(value).toUpperCase());
        }

        @Override
        public Class<?> valueType() {
            return String.class;
        }
    }

    private final JSON WRITER = JSON.builder()
            .register(new JacksonJrExtension() {
                @Override
                protected void register(ExtensionContext ctxt) {
                    ctxt.insertModifier(new ReaderWriterModifier() {
                        @Override
                        public ValueWriter overrideStandardValueWriter(JSONWriter writeContext, Class<?> type, int stdTypeId) {
                            if (Path.class.isAssignableFrom(type)) {
                                return new PathWriter();
                            }
                            if (type == String.class) {
                                return new UpperCaseWriter();
                            }
                            return null;
                        }
                    });
                    ctxt.insertProvider(new ReaderWriterProvider() {
                        @Override
                        public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
                            if (Path.class.isAssignableFrom(type)) {
                                return new PathWriter();
                            }
                            return null;
                        }
                    });
                }
            }).build();

    // [jackson-jr#112]
    @Test
    public void testMultipleFieldOverrides() throws Exception
    {
        TestBean112 input = new TestBean112();
        input.p1 = Paths.get("some/path");
        input.p2 = Paths.get("some/other/path");
        String json = WRITER.asString(input);
        assertEquals(a2q("{'p1':'some/path','p2':'some/other/path'}"), json);
    }

    @Test
    public void testMultipleStringFieldOverrides() throws Exception
    {
        StringBean112 input = new StringBean112();
        input.s1 = "abc";
        input.s2 = "def";
        input.s3 = "g";
        String json = WRITER.asString(input);
        assertEquals(a2q("{'s1':'ABC','s2':'DEF','s3':'G'}"), json);
    }
}
