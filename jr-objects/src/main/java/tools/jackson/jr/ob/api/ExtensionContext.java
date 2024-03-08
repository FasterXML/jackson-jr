package tools.jackson.jr.ob.api;

import tools.jackson.core.TreeCodec;
import tools.jackson.jr.ob.JSON;

/**
 * API that lets {@link tools.jackson.jr.ob.JacksonJrExtension}s to register handlers
 * it needs to.
 */
public abstract class ExtensionContext
{
    // // // Config access

    /**
     * Method for checking whether given {@code JSON.Feature} is enabled.
     *
     * @param feature Feature to check
     *
     * @return True if given {@code JSON.Feature} is enabled; {@code false} if not
     *
     * @since 2.17
     */
    public abstract boolean isEnabled(JSON.Feature feature);
    
    // // // Override of (and access to) singleton handlers

    /**
     * Method for setting {@link TreeCodec} to use, replacing codec that was formerly configured
     * (if any).
     *
     * @param tc (optional) Tree Codec to use, or {@code null} for removing currently configured one
     *
     * @return This context, to allow call chaining
     */
    public abstract ExtensionContext setTreeCodec(TreeCodec tc);

    /**
     * @return TreeCodec currently configured to be used, if any ({@code null} if none).
     */
    public abstract TreeCodec treeCodec();
    
    // // // Addition of chainable handlers
    
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
