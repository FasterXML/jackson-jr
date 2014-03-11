package com.fasterxml.jackson.jr.ob.impl;

/**
 * Class that contains information about dynamically introspected
 * Bean types.
 */
public class BeanDefinition
{
    protected final Class<?> _type;

    protected BeanProperty[] _properties;

    public BeanDefinition(Class<?> type,
            BeanProperty[] props)
    {
        _type = type;
        _properties = props;
    }
}
