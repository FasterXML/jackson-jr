package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonGenerator;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.*;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

public class ValueWriterModifier112Test extends TestBase
{
    static class TestBean112 {
        public Path p1;
        public Path p2;
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

    // [jackson-jr#112]
    public void testMultipleFieldOverrides() throws Exception
    {
        TestBean112 input = new TestBean112();
        input.p1 = Paths.get("some/path");
        input.p2 = Paths.get("some/other/path");

        JSON writer = JSON.builder()
            .register(new JacksonJrExtension() {
                @Override
                protected void register(ExtensionContext ctxt) {
                    ctxt.insertModifier(new ReaderWriterModifier() {
                        @Override
                        public ValueWriter overrideStandardValueWriter(JSONWriter writeContext, Class<?> type, int stdTypeId) {
                            if (type == Path.class) {
                                return new PathWriter();
                            }
                            return super.overrideStandardValueWriter(writeContext, type, stdTypeId);
                        }
                    });
                    ctxt.insertProvider(new ReaderWriterProvider() {
                        @Override
                        public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
                            if (Path.class.isAssignableFrom(type)) {
                                return new PathWriter();
                            }
                            return super.findValueWriter(writeContext, type);
                        }
                    });
                }
            }).build();
        String json = writer.asString(input);
        assertEquals(a2q("{'p1':'some/path','p2':'some/other/path'}"), json);
    }
}
