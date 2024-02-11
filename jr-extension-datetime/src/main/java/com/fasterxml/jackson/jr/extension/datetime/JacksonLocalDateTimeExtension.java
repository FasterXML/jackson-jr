package com.fasterxml.jackson.jr.extension.datetime;

import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

public class JacksonLocalDateTimeExtension extends JacksonJrExtension {
    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertProvider(new LocalDateTimeReaderWriterProvider());
    }
}
