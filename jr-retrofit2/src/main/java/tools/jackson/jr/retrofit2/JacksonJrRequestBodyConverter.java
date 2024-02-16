package tools.jackson.jr.retrofit2;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import retrofit2.Converter;

import tools.jackson.jr.ob.JSON;

public class JacksonJrRequestBodyConverter<T> implements Converter<T, RequestBody>
{
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    protected static final String PROTOCOL_CHARSET = "utf-8";

    protected final JSON _jr;

    public JacksonJrRequestBodyConverter(JSON jr) {
        _jr = jr;
    }

    @Override
    public RequestBody convert(T value) {

        byte[] bytes = _jr.asBytes(value);
        return RequestBody.create(MEDIA_TYPE, bytes);
    }
}
