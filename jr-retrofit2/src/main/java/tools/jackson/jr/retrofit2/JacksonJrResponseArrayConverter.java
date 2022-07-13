package tools.jackson.jr.retrofit2;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;

import retrofit2.Converter;
import tools.jackson.jr.ob.JSON;

public class JacksonJrResponseArrayConverter<T> implements Converter<ResponseBody, List<T>>
{
    protected final JSON _jr;
    protected final Class<T> _type;

    public JacksonJrResponseArrayConverter(JSON jr, Class<T> type) {
        _jr = jr;
        _type = type;
    }

    @Override
    public List<T> convert(ResponseBody value) throws IOException {
        return _jr.listOfFrom(_type, value.bytes());
    }
}
