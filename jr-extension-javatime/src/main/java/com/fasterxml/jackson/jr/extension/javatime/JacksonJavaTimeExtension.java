package com.fasterxml.jackson.jr.extension.javatime;

import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

public class JacksonJavaTimeExtension extends JacksonJrExtension {
    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertProvider(new JacksonJavaTimeReaderWriterProvider());
    }
}
