package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JacksonJrExtension;
import com.fasterxml.jackson.jr.ob.api.ExtensionContext;

/**
 * Helper object that provides limited support for Jackson core annotations.
 *<p>
 * Set of annotations supported includes:
 *<ul>
 * <li>{link com.fasterxml.jackson.annotation.JsonAlias}: supported on accessors
 *  (fields, getters, setters)
 *  </li>
 * <li>{@link com.fasterxml.jackson.annotation.JsonPropertyOrder}: supported on classes
 *  </li>
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
    /**
     * Default visibility settings for Jackson-jr: in addition to being defaults commonly
     * used as the base for creating alternative settings. For example,
     * to allow auto-detection of ALL fields regardless visibility, but none
     * of setters (without annotation) you could use
     *<pre>
     *  JacksonAnnotationExtension ext = JacksonAnnotationExtension.builder()
     *     .withVisibility(JacksonAnnotationExtension.DEFAULT_VISIBILITY
     *         .withFieldVisibility(Visibility.ANY)
     *         .withSetterVisibility(Visibility.NONE)
     *      ).build();
     *</pre>
     *<p>
     * Default visibility settings are {@code Visibility.PUBLIC_ONLY} for all other accessors
     * except for setters, for which {@code Visibility.ANY} is used.
     * In addition, following core settings are also considered:
     *<ul>
     * <li>Fields: must also have {@link com.fasterxml.jackson.jr.ob.JSON.Feature#USE_FIELDS} enabled
     *    otherwise {@code Visibility.NONE} is used
     *  </li>
     * <li>Is-getters: must also have {@link com.fasterxml.jackson.jr.ob.JSON.Feature#USE_IS_GETTERS} enabled
     *    otherwise {@code Visibility.NONE} is used
     *  </li>
     * <li>Creators: never auto-detected so setting irrelevant
     *  </li>
     *</ul>
     */
    public final static JsonAutoDetect.Value DEFAULT_VISIBILITY =
            JsonAutoDetect.Value.construct(
                    JsonAutoDetect.Visibility.PUBLIC_ONLY, // fields
                    JsonAutoDetect.Visibility.PUBLIC_ONLY, // getters
                    JsonAutoDetect.Visibility.PUBLIC_ONLY, // isGetters
                    JsonAutoDetect.Visibility.PUBLIC_ONLY, // setters
                    JsonAutoDetect.Visibility.NONE, // other creators,
                    JsonAutoDetect.Visibility.NONE // scalar creators,
            );

    /**
     * Builder class for configuring resulting {@link JacksonAnnotationExtension}.
     */
    public static class Builder
    {
        public JsonAutoDetect.Value visibility = DEFAULT_VISIBILITY;

        public JacksonAnnotationExtension build() {
            return new JacksonAnnotationExtension(this);
        }

        /**
         * Method for setting visibility settings to specified parameter and returning
         * resulting builder instance.
         *
         * @param allVisibility Visibility settings to use, or {@code null} to indicate
         *    "use default visibility" (see {@link JacksonAnnotationExtension#DEFAULT_VISIBILITY}).
         *
         * @return Builder with specified visibility settings
         */
        public Builder withVisibility(JsonAutoDetect.Value allVisibility) {
            visibility = (allVisibility == null) ? DEFAULT_VISIBILITY : allVisibility;
            return this;
        }
    }

    protected final AnnotationBasedValueRWModifier _modifier;

    protected JacksonAnnotationExtension() {
        _modifier = new AnnotationBasedValueRWModifier(DEFAULT_VISIBILITY);
    }

    protected JacksonAnnotationExtension(Builder b) {
        _modifier = new AnnotationBasedValueRWModifier(b.visibility);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void register(ExtensionContext ctxt) {
        ctxt.insertModifier(_modifier);
    }
}
