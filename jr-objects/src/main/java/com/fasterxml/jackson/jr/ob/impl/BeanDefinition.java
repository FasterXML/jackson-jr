package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

/**
 * Class that contains information about dynamically introspected
 * Bean types.
 */
public class BeanDefinition
    extends ValueReader // so we can chain calls for Collections, arrays
{
    protected final Class<?> _type;

    protected final BeanProperty[] _properties; // for serialization
    protected final Map<String,BeanProperty> _propsByName; // for deserialization

    protected final Constructor<?> _defaultCtor;
    protected final Constructor<?> _stringCtor;
    protected final Constructor<?> _longCtor;

    /**
     * Constructors used for serialization use case
     */
    public BeanDefinition(Class<?> type, BeanProperty[] props)
    {
        _type = type;
        _properties = props;
        _propsByName = null;
        _defaultCtor = null;
        _stringCtor = null;
        _longCtor = null;
    }

    /**
     * Constructors used for deserialization use case
     */
    public BeanDefinition(Class<?> type, Map<String, BeanProperty> props,
            Constructor<?> defaultCtor, Constructor<?> stringCtor, Constructor<?> longCtor)
    {
        _type = type;
        _properties = null;
        _propsByName = props;
        _defaultCtor = defaultCtor;
        _stringCtor = stringCtor;
        _longCtor = longCtor;
    }
    
    public BeanProperty[] properties() { return _properties; }
    public Map<String,BeanProperty> propertiesByName() { return _propsByName; }

    public BeanProperty findProperty(String name) {
        return _propsByName.get(name);
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
                    for (; (t = p.nextToken()) == JsonToken.FIELD_NAME; ) {
                        String fieldName = p.getCurrentName();
                        BeanProperty prop = findProperty(fieldName);
                        if (prop == null) {
                            handleUnknown(r, p, fieldName);
                            continue;
                        }
                        p.nextToken();
                        ValueReader vr = prop.getReader();
                        if (vr == null) { // lazily created
                            vr = constructPropertyReader(r, prop);
                        }
                        Object value = vr.read(r, p);
                        prop.setValueFor(bean, value);
                    }
                    return bean;
                }
            default:
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

    protected ValueReader constructPropertyReader(JSONReader r, BeanProperty prop)
    {
        int propTypeId = prop.getTypeId();
        // need to dynamically resolve bean type refs
        if (propTypeId == TypeDetector.SER_UNKNOWN) {
        //        _resolveProperty(prop)
            propTypeId = r._typeDetector.findFullType(prop._rawType);
            if (propTypeId != TypeDetector.SER_UNKNOWN) { 
                prop.overridTypeId(propTypeId);
            }
        }

        /*
        if (propTypeId < 0) {
            value = r._typeDetector.getBeanDefinition(propTypeId).read(reader, parser);
        } else {
            Class<?> rawType = 
            value = r._readSimpleValue(prop.getType(), propTypeId);
        }
        */
        return null;
    }
}
