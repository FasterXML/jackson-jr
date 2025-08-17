package tools.jackson.jr.extension.javatime;

import tools.jackson.jr.ob.JacksonJrExtension;
import tools.jackson.jr.ob.api.ExtensionContext;

public class JacksonJrJavaTimeExtension extends JacksonJrExtension {
    final static JavaTimeReaderWriterProvider DEFAULT_RW_PROVIDER = new JavaTimeReaderWriterProvider();

    private JavaTimeReaderWriterProvider _readerWriterProvider = DEFAULT_RW_PROVIDER;

    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertProvider(_readerWriterProvider);
    }

    public JacksonJrJavaTimeExtension with(JavaTimeReaderWriterProvider p) {
        _readerWriterProvider = p;
        return this;
    }
}
