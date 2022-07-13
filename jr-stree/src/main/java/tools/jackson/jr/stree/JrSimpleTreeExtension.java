package tools.jackson.jr.stree;

import tools.jackson.jr.ob.JacksonJrExtension;
import tools.jackson.jr.ob.api.ExtensionContext;

/**
 * Simple {@link JacksonJrExtension} for registering "simple Tree Model"
 * implementation ({@link JacksonJrsTreeCodec}).
 *
 * @see JacksonJrsTreeCodec
 */
public class JrSimpleTreeExtension
    extends JacksonJrExtension
{
    protected final JacksonJrsTreeCodec _codec;

    public JrSimpleTreeExtension() {
        this(new JacksonJrsTreeCodec());
    }

    public JrSimpleTreeExtension(JacksonJrsTreeCodec tc) {
        _codec = tc;
    }
    
    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.setTreeCodec(_codec);
    }
}
