package com.fasterxml.jackson.jr.extension.javatime.date;

import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

public class JacksonJrJavaDateExtension extends JacksonJrExtension {
    static final JavaDateReaderWriterProvider DEFAULT_RW_PROVIDER = new JavaDateReaderWriterProvider();

    private JavaDateReaderWriterProvider readerWriterProvider = DEFAULT_RW_PROVIDER;

    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertProvider(readerWriterProvider);
    }

    public JacksonJrJavaDateExtension with(JavaDateReaderWriterProvider p) {
        readerWriterProvider = p;
        return this;
    }
}
