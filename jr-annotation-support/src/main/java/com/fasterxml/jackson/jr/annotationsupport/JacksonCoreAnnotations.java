package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Helper object that provides limited support for Jackson core annotations.
 *<p>
 * Set of annotations supported includes:
 *<ul>
 * <li>{@code @JsonIgnore}
 *  </li>
 * <li>{@code @JsonIgnoredProperties}
 *  </li>
 * <li>{@code @JsonProperty}
 *  </li>
 * <li>{@code @JsonPropertyOrder}
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
