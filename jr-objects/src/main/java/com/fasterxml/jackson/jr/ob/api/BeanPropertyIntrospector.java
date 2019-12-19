package com.fasterxml.jackson.jr.ob.api;

import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;

/**
 * API to implement to apply modifications to customize discovery of properties
 * of Beans (aka POJOs) for purpose of reading (deserialization) and writing
 * (serialization).
 *<p>
 * Usage is a two-phase process: after introspector has been configured to be used by
 * {@link com.fasterxml.jackson.jr.ob.JSON}, 
 *
 * @since 2.11
 */
public abstract class BeanPropertyIntrospector
{
    // // // Instance creation

    public BeanPropertyIntrospector instanceForDeserialization(JSONReader r, Class<?> pojoType) {
        // for stateless implementations this is fine
        return this;
    }

    public BeanPropertyIntrospector instanceForSerialization(JSONWriter w, Class<?> pojoType) {
        // for stateless implementations this is fine
        return this;
    }


}
