package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * Class that contains information about dynamically introspected
 * Bean types, to be able to deserialize (read) JSON into a POJO
 * and serialize (write) POJO as JSON.
 */
public class BeanReader
    extends ValueReader // so we can chain calls for Collections, arrays
{
    protected final Map<String,BeanPropertyReader> _propsByName; // for deserialization

    // @since 2.11
    protected final Set<String> _ignorableNames;

    protected final Constructor<?> _defaultCtor;
    protected final Constructor<?> _stringCtor;
    protected final Constructor<?> _longCtor;

    /**
     * Constructors used for deserialization use case
     */
    public BeanReader(Class<?> type, Map<String, BeanPropertyReader> props,
            Constructor<?> defaultCtor, Constructor<?> stringCtor, Constructor<?> longCtor,
            Set<String> ignorableNames)
    {
        super(type);
        _propsByName = props;
        _defaultCtor = defaultCtor;
        _stringCtor = stringCtor;
        _longCtor = longCtor;
        _ignorableNames = ignorableNames;
    }

    @Deprecated // since 2.11
    public BeanReader(Class<?> type, Map<String, BeanPropertyReader> props,
            Constructor<?> defaultCtor, Constructor<?> stringCtor, Constructor<?> longCtor) {
        this(type, props, defaultCtor, stringCtor, longCtor, Collections.<String>emptySet());
    }

    public Map<String,BeanPropertyReader> propertiesByName() { return _propsByName; }

    public BeanPropertyReader findProperty(String name) {
        return _propsByName.get(name);
    }

    @Override
    public Object readNext(JSONReader r, JsonParser p) throws IOException
    {
        JsonToken t = p.nextToken();
        try {
            switch (t) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                return create(p.getText());
            case VALUE_NUMBER_INT:
                return create(p.getLongValue());
            case START_OBJECT:
                {
                    Object bean = create();
                    String propName;
                    
                    for (; (propName = p.nextFieldName()) != null; ) {
                        BeanPropertyReader prop = findProperty(propName);
                        if (prop == null) {
                            handleUnknown(r, p, propName);
                            continue;
                        }
                        ValueReader vr = prop.getReader();
                        prop.setValueFor(bean, vr.readNext(r, p));
                    }
                    // also verify we are not confused...
                    if (!p.hasToken(JsonToken.END_OBJECT)) {
                        throw _reportProblem(p);
                    }                    
                    
                    return bean;
                }
            default:
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, "Failed to create an instance of "
                    +_valueType.getName()+" due to ("+e.getClass().getName()+"): "+e.getMessage(),
                    e);
        }
        throw JSONObjectException.from(p,
                "Can not create a "+_valueType.getName()+" instance out of "+_tokenDesc(p));
    }
    
    /**
     * Method used for deserialization; will read an instance of the bean
     * type using given parser.
     */
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException
    {
        JsonToken t = p.getCurrentToken();

        try {
            switch (t) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                return create(p.getText());
            case VALUE_NUMBER_INT:
                return create(p.getLongValue());
            case START_OBJECT:
                {
                    Object bean = create();
                    String propName;
                    
                    for (; (propName = p.nextFieldName()) != null; ) {
                        BeanPropertyReader prop = findProperty(propName);
                        if (prop == null) {
                            handleUnknown(r, p, propName);
                            continue;
                        }
                        final Object value = prop.getReader().readNext(r, p);
                        prop.setValueFor(bean, value);
                    }
                    // also verify we are not confused...
                    if (!p.hasToken(JsonToken.END_OBJECT)) {
                        throw _reportProblem(p);
                    }                    
                    
                    return bean;
                }
            default:
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, e,
                    "Failed to create an instance of %s due to (%s): %s",
                    _valueType.getName(), e.getClass().getName(), e.getMessage());
        }
        throw JSONObjectException.from(p, "Can not create a %s instance out of %s",
                _valueType.getName(), _tokenDesc(p));
    }
    
    protected Object create() throws Exception {
        if (_defaultCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have default constructor to use");
        }
        return _defaultCtor.newInstance();
    }
    
    protected Object create(String str) throws Exception {
        if (_stringCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have single-String constructor to use");
        }
        return _stringCtor.newInstance(str);
    }

    protected Object create(long l) throws Exception {
        if (_longCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have single-long constructor to use");
        }
        return _longCtor.newInstance(l);
    }

    protected void handleUnknown(JSONReader reader, JsonParser parser, String fieldName) throws IOException {
        if (JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY.isEnabled(reader._features)) {
            // 20-Jan-2020, tatu: With optional annotation support, may have "known ignorable"
            //    that usually should behave as if safely ignorable
            if (!_ignorableNames.contains(fieldName)) {
                throw JSONObjectException.from(parser, "Unrecognized JSON property '"
                        +fieldName+"' for Bean type "+_valueType.getName());
            }
        }
        parser.nextToken();
        parser.skipChildren();
    }

    protected IOException _reportProblem(JsonParser p) {
        return JSONObjectException.from(p, "Unexpected token "+p.getCurrentToken()+"; should get FIELD_NAME or END_OBJECT");
    }
}
