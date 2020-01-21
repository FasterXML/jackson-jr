package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.jr.ob.JSON;

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
 */
public class JacksonCoreAnnotations
{
    public static class Builder {
        public JacksonCoreAnnotations build() {
            return new JacksonCoreAnnotations();
        }
    }

    protected final AnnotationBasedValueRWModifier _modifier;
    
    protected JacksonCoreAnnotations() {
        _modifier = new AnnotationBasedValueRWModifier();
    }

    public static Builder builder() {
        return new Builder();
    }

    public JSON addAnnotationSupport(JSON json) {
        return json.with(_modifier);
    }
}
