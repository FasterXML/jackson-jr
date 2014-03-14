package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Class that contains information about dynamically introspected
 * Bean types.
 */
public class BeanDefinition
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

    public BeanProperty findProperty(String name) {
        return _propsByName.get(name);
    }
    
    public Object create() throws Exception {
        if (_defaultCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have default constructor to use");
        }
        return _defaultCtor.newInstance();
    }
    
    public Object create(String str) throws Exception {
        if (_stringCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have single-String constructor to use");
        }
        return _stringCtor.newInstance(str);
    }

    public Object create(long l) throws Exception {
        if (_longCtor == null) {
            throw new IllegalStateException("Class "+_type.getName()+" does not have single-long constructor to use");
        }
        return _longCtor.newInstance(l);
    }
}
