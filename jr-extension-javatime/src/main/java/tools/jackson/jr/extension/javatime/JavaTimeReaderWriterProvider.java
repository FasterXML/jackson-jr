package tools.jackson.jr.extension.javatime;

import tools.jackson.jr.ob.api.*;
import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provider for {@link ValueReader}s and {@link ValueWriter}s for Date/Time
 * types supported by Java Time Extension.
 */
public class JavaTimeReaderWriterProvider extends ReaderWriterProvider
{
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public JavaTimeReaderWriterProvider() { }

    @Override
    public ValueReader findValueReader(JSONReader readContext, Class<?> type) {
        return LocalDateTime.class.isAssignableFrom(type) ? new LocalDateTimeValueReader(dateTimeFormatter) : null;
    }

    @Override
    public ValueWriter findValueWriter(JSONWriter writeContext, Class<?> type) {
        return LocalDateTime.class.isAssignableFrom(type) ? new LocalDateTimeValueWriter(dateTimeFormatter) : null;
    }

    /**
     * Method for reconfiguring {@link DateTimeFormatter} used for reading/writing
     * following Date/Time value types:
     *<ul>
     * <li>{@code java.time.LocalDateTime}
     *  </li>
     *</ul>
     * 
     * @param formatter
     *
     * @return This provider instance for call chaining
     */
    public JavaTimeReaderWriterProvider withDateTimeFormatter(DateTimeFormatter formatter) {
        dateTimeFormatter = formatter;
        return this;
    }
}
