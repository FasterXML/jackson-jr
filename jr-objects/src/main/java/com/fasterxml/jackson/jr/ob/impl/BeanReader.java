package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Class that contains information about dynamically introspected
 * Bean types, to be able to deserialize (read) JSON into a POJO
 * and serialize (write) POJO as JSON.
 */
public class BeanReader
    extends ValueReader // so we can chain calls for Collections, arrays
{
    protected final Class<?> _type;

    protected final Map<String,BeanPropertyReader> _propsByName; // for deserialization

    protected final Constructor<?> _defaultCtor;
    protected final Constructor<?> _stringCtor;
    protected final Constructor<?> _longCtor;

    /**
     * Constructors used for deserialization use case
     */
    public BeanReader(Class<?> type, Map<String, BeanPropertyReader> props,
            Constructor<?> defaultCtor, Constructor<?> stringCtor, Constructor<?> longCtor)
    {
        _type = type;
        _propsByName = props;
        _defaultCtor = defaultCtor;
        _stringCtor = stringCtor;
        _longCtor = longCtor;
    }

    public Map<String,BeanPropertyReader> propertiesByName() { return _propsByName; }

    public BeanPropertyReader findProperty(String name) {
        return _propsByName.get(name);
    }

    /**
     * Method used for deserialization; will read an instance of the bean
     * type using given parser.
     */
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException
    {
        try {
            if (p.isExpectedStartObjectToken()) {
                return _readBean(r, p, create());
            }
            JsonToken t = p.currentToken();
            if (t != null) {
                switch (t) {
                case VALUE_NULL:
                    return null;
                case VALUE_STRING:
                    return create(p.getText());
                case VALUE_NUMBER_INT:
                    return create(p.getLongValue());
                default:
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, e,
                    "Failed to create an instance of %s due to (%s): %s",
                    _type.getName(), e.getClass().getName(), e.getMessage());
        }
        throw JSONObjectException.from(p, "Can not create a %s instance out of %s",
                _type.getName(), _tokenDesc(p));
    }
    
    @Override
    public Object readNext(JSONReader r, JsonParser p) throws IOException
    {
        try {
            JsonToken t = p.nextToken();
            if (t == JsonToken.START_OBJECT) {
                return _readBean(r, p, create());
            }
            if (t != null) {
                switch (t) {
                case VALUE_NULL:
                    return null;
                case VALUE_STRING:
                    return create(p.getText());
                case VALUE_NUMBER_INT:
                    return create(p.getLongValue());
                default:
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, "Failed to create an instance of "
                    +_type.getName()+" due to ("+e.getClass().getName()+"): "+e.getMessage(),
                    e);
        }
        throw JSONObjectException.from(p,
                "Can not create a "+_type.getName()+" instance out of "+_tokenDesc(p));
    }

    private final Object _readBean(JSONReader r, JsonParser p, final Object bean) throws IOException
    {
        String propName;
        BeanPropertyReader prop;

        // Unroll first 6 rounds (similar to databind)
        do {
            // Element 1
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // Element 2
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // Element 3
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // Element 4
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // Element 5
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // Element 6
            if ((propName = p.nextFieldName()) == null) { break; }
            if ((prop = findProperty(propName)) == null) {
                return _readWithUnknown(r, p, bean, propName);
            }
            prop.setValueFor(bean, prop.getReader().readNext(r, p));

            // and if more, just loop
            for (; (propName = p.nextFieldName()) != null; ) {
                prop = findProperty(propName);
                if (prop == null) {
                    handleUnknown(r, p, propName);
                    continue;
                }
                prop.setValueFor(bean, prop.getReader().readNext(r, p));
            }
        } while (false);
        // also verify we are not confused...
        if (!p.hasToken(JsonToken.END_OBJECT)) {
            throw _reportProblem(p);
        }                    
        
        return bean;
    }

    private final Object _readWithUnknown(JSONReader r, JsonParser p,
            final Object bean, String propName)
        throws IOException
    {
        // first, skip current property
        handleUnknown(r, p, propName);
        // then do the rest with looping
        for (; (propName = p.nextFieldName()) != null; ) {
            BeanPropertyReader prop = findProperty(propName);
            if (prop == null) {
                handleUnknown(r, p, propName);
                continue;
            }
            ValueReader vr = prop.getReader();
            prop.setValueFor(bean, vr.readNext(r, p));
        }
        if (!p.hasToken(JsonToken.END_OBJECT)) {
            throw _reportProblem(p);
        }                    
        
        return bean;
    }

    protected Object create() throws Exception {
        if (_defaultCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have default constructor to use");
        }
        return _defaultCtor.newInstance();
    }
    
    protected Object create(String str) throws Exception {
        if (_stringCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have single-String constructor to use");
        }
        return _stringCtor.newInstance(str);
    }

    protected Object create(long l) throws Exception {
        if (_longCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have single-long constructor to use");
        }
        return _longCtor.newInstance(l);
    }

    protected void handleUnknown(JSONReader reader, JsonParser parser, String fieldName) throws IOException {
        if (JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY.isEnabled(reader._features)) {
            throw JSONObjectException.from(parser, "Unrecognized JSON property '"
                    +fieldName+"' for Bean type "+_type.getName());
        }
        parser.nextToken();
        parser.skipChildren();
    }

    protected IOException _reportProblem(JsonParser p) {
        return JSONObjectException.from(p, "Unexpected token "+p.currentToken()+"; should get FIELD_NAME or END_OBJECT");
    }
}
