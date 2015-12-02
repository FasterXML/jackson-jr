package com.fasterxml.jackson.jr.retrofit2;

import com.fasterxml.jackson.jr.ob.JSON;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import retrofit.Converter;

public class JacksonJrResponseConverter<T> implements Converter<ResponseBody, T>
{
    protected final JSON _jr;
    protected final Class<T> _type;

    public JacksonJrResponseConverter(JSON jr, Class<T> t) {
        _jr = jr;
        _type = t;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        return _jr.beanFrom(_type, value.bytes());
    }
}
