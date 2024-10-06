package com.fasterxml.jackson.jr.extension.javatime.date;

import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

import java.util.Date;

public class JavaDateReaderWriterProvider extends ReaderWriterProvider {
    public JavaDateReaderWriterProvider() {
    }

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        return Date.class.isAssignableFrom(type) ? new DateValueReader() : null;
    }

    @Override
    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        return Date.class.isAssignableFrom(type) ? new DateValueWriter() : null;
    }
}
