package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Method;

public class BeanProperty
{
    protected final String _name;
    
    protected final Method _readMethod, _writeMethod;

    public BeanProperty(String name,
            Method readMethod, Method writeMethod)
    {
        _name = name;
        _readMethod = readMethod;
        _writeMethod = writeMethod;
    }
}
