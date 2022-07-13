package tools.jackson.jr.retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import tools.jackson.jr.ob.JSON;

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
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit)
    {
        super.responseBodyConverter(type, annotations, retrofit);

        //if same as class type return as class, otherwise, return as list
        if (_type == type) {
            return new JacksonJrResponseConverter<T>(_jr, _type);
        }
        return new JacksonJrResponseArrayConverter<T>(_jr, _type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit)
    {
        super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
        return new JacksonJrRequestBodyConverter<T>(_jr);
    }
}
