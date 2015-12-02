package com.fasterxml.jackson.jr.retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.fasterxml.jackson.jr.ob.JSON;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import retrofit.Converter;

public class JacksonJrConverter<T> extends Converter.Factory
{
    protected final JSON _jr;
    protected final Class<T> _type;

    public JacksonJrConverter(Class<T> tClass) {
        this(JSON.std, tClass);
    }

    public JacksonJrConverter(JSON jr, Class<T> t) {
        super();
        _jr = jr;
        _type = t;
    }
    
    @Override
    public Converter<ResponseBody, ?> fromResponseBody(Type type, Annotation[] annotations) {
        super.fromResponseBody(type, annotations);

        //if same as class type return as class, otherwise, return as list
        if (_type == type) {
            return new JacksonJrResponseConverter<T>(_jr, _type);
        }
        return new JacksonJrResponseArrayConverter<T>(_jr, _type);
    }

    @Override
    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        super.toRequestBody(type, annotations);
        return new JacksonJrRequestBodyConverter<T>(_jr);
    }
}
