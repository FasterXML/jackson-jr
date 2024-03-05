package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

/**
 * Simple {@link JacksonJrExtension} for registering "simple Tree Model"
 * implementation ({@link JacksonJrsTreeCodec}).
 *
 * @see JacksonJrsTreeCodec
 *
 * @since 2.11
 */
public class JrSimpleTreeExtension
    extends JacksonJrExtension
{
    protected final JacksonJrsTreeCodec _codec;

    public JrSimpleTreeExtension() {
        this(new JacksonJrsTreeCodec());
    }

    public JrSimpleTreeExtension(JSON config) {
        this(new JacksonJrsTreeCodec(config));
    }

    public JrSimpleTreeExtension(JacksonJrsTreeCodec tc) {
        _codec = tc;
    }
    
    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.setTreeCodec(_codec);
    }
}
