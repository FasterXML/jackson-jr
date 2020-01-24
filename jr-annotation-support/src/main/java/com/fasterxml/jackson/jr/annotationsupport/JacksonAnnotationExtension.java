package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JacksonJrExtension;

/**
 * Helper object that provides limited support for Jackson core annotations.
 *<p>
 * Set of annotations supported includes:
 *<ul>
 * <li>{link com.fasterxml.jackson.annotation.JsonIgnore}: supported on accessors
 *  (fields, getters, setters)
 *  </li>
 * <li>{@link com.fasterxml.jackson.annotation.JsonIgnoreProperties}: supported on classes,
 *   but not on accessors
 *  </li>
 * <li>{@link com.fasterxml.jackson.annotation.JsonProperty} supported on accessors
 *  (fields, getters, setters) to specify explicit inclusion, name override. Other properties
 *  ({@code index}, {@code required}) not supported.
 *  </li>
 * <li>{@link com.fasterxml.jackson.annotation.JsonPropertyOrder}: supported on classes,
 *    but not on accessors
 *  </li>
 *</ul>
 *<p>
 * Usage is done by registering this extension with {@link JSON}, usually like:
 *<pre>
 *   JSON json = JSON.std.register(JacksonAnnotationExtension.builder()
 *       // possible configuration calls
 *       .build());
 *</pre>
 */
public class JacksonAnnotationExtension
    extends JacksonJrExtension
{
    public static class Builder {
        public JacksonAnnotationExtension build() {
            return new JacksonAnnotationExtension();
        }
    }

    protected final AnnotationBasedValueRWModifier _modifier;
    
    protected JacksonAnnotationExtension() {
        _modifier = new AnnotationBasedValueRWModifier();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected JSON register(JSON json) {
        return json.with(_modifier);
    }
}
