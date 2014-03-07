package com.fasterxml.jackson.jr.ob.impl;

import java.beans.MethodDescriptor;
import java.lang.reflect.Constructor;

/**
 * Class that contains information about dynamically introspected
 * Bean types.
 */
public class BeanDefinition
{
    protected final Class<?> _type;
    
    protected Constructor<?> _ctorDefault;

    public BeanDefinition(Class<?> type,
            Constructor<?> defCtor,
            MethodDescriptor[] methods)
    {
        _type = type;
    }
}
