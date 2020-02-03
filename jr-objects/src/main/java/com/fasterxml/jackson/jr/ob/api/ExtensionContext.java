package com.fasterxml.jackson.jr.ob.api;

/**
 * API that lets {@link com.fasterxml.jackson.jr.ob..JacksonJrExtension}s to register handlers
 * it needs to.
 *
 * @since 2.11
 */
public abstract class ExtensionContext
{
    /**
     * Method for inserting specified {@link ReaderWriterProvider} as the highest priority
     * provider (that is, having higher precedence than anything registered so far)
     *
     * @param provider Provider to register
     *
     * @return This context, to allow call chaining
     */
    public abstract ExtensionContext insertProvider(ReaderWriterProvider provider);

    /**
     * Method for inserting specified {@link ReaderWriterProvider} as the lowest priority
     * provider (that is, having lower precedence than anything registered so far)
     *
     * @param provider Provider to register
     *
     * @return This context, to allow call chaining
     */
    public abstract ExtensionContext appendProvider(ReaderWriterProvider provider);

    /**
     * Method for inserting specified {@link ReaderWriterModifier} as the highest priority
     * modifier (that is, being called before any other modifiers registered).
     *
     * @param modifier Modifier to register
     *
     * @return This context, to allow call chaining
     */
    public abstract ExtensionContext insertModifier(ReaderWriterModifier modifier);

    /**
     * Method for inserting specified {@link ReaderWriterModifier} as the lowest priority
     * modifier (that is, being called after all other modifiers registered).
     *
     * @param modifier Modifier to register
     *
     * @return This context, to allow call chaining
     */
    public abstract ExtensionContext appendModifier(ReaderWriterModifier modifier);
}
