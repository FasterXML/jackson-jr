package com.fasterxml.jackson.jr.ob;

import com.fasterxml.jackson.jr.ob.api.ExtensionContext;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterProvider;
import com.fasterxml.jackson.jr.ob.api.ValueReader;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;

/**
 * Simple interface that processing additions can implement to be easily pluggable
 * to main Jackson jr handler, {@link com.fasterxml.jackson.jr.ob.JSON}.
 * Extensions typically provide things like custom readers ({@link ValueReader})
 * and writers ({@link ValueWriter}) (via {@link ReaderWriterProvider}), and
 * modifers ({@link ReaderWriterModifier}) that allow customizing aspects of
 * JSON reading and writing.
 */
public abstract class JacksonJrExtension
{
    /**
     * Method called by {@link JSON} to let extension register handlers
     * it wants to.
     *
     * @param ctxt Context that allows extension to register handlers
     */
    protected abstract void register(ExtensionContext ctxt);
}
