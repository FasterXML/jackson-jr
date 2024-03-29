package com.fasterxml.jackson.jr.extension.javatime;

import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

public class JacksonJrJavaTimeExtension extends JacksonJrExtension {
    final static JavaTimeReaderWriterProvider DEFAULT_RW_PROVIDER = new JavaTimeReaderWriterProvider();

    private JavaTimeReaderWriterProvider readerWriterProvider = DEFAULT_RW_PROVIDER;

    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertProvider(readerWriterProvider);
    }

    public JacksonJrJavaTimeExtension with(JavaTimeReaderWriterProvider p) {
        readerWriterProvider = p;
        return this;
    }
}
