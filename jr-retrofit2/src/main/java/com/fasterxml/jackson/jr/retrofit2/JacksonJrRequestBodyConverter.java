package com.fasterxml.jackson.jr.retrofit2;

import com.fasterxml.jackson.jr.ob.JSON;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

import retrofit.Converter;

/**
 * Created by Gulajava Ministudio on 11/18/15.
 */
public class JacksonJrRequestBodyConverter<T> implements Converter<T, RequestBody>
{
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    protected static final String PROTOCOL_CHARSET = "utf-8";

    protected final JSON _jr;

    public JacksonJrRequestBodyConverter(JSON jr) {
        _jr = jr;
    }

    @Override
    public RequestBody convert(T value) throws IOException {

        byte[] bytes = _jr.asBytes(value);
        return RequestBody.create(MEDIA_TYPE, bytes);
    }
}
