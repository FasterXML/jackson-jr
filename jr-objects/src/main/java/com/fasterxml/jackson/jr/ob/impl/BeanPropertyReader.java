package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * @since 2.8 (formerly part of `BeanDefinition`)
 */
public final class BeanPropertyReader
{
    protected final String _name;

    /**
     * For non-trivial non-bean types
     */
    protected final ValueReader _valueReader;

    /**
     * Setter method to use, if any; null if none
     */
    protected final Method _setter;

    /**
     * Field to assign value to, if no setter method defined; null if none
     */
    protected final Field _field;

    public BeanPropertyReader(String name, Field f, Method setter) {
        if ((f == null) && (setter == null)) {
            throw new IllegalArgumentException("Both `field` and `setter` can not be null");
        }
        _name = name;
        _field = f;
        _setter = setter;
        _valueReader = null;
    }

    protected BeanPropertyReader(BeanPropertyReader src, ValueReader vr) {
        _name = src._name;
        _field = src._field;
        _setter = src._setter;
        _valueReader = vr;
    }

    public BeanPropertyReader withReader(ValueReader vr) {
        return new BeanPropertyReader(this, vr);
    }

    public Type genericSetterType() {
        if (_setter != null) {
            return _setter.getGenericParameterTypes()[0];
        }
        return _field.getGenericType();
    }

    public Class<?> rawSetterType() {
        if (_setter != null) {
            return _setter.getParameterTypes()[0];
        }
        return _field.getType();
    }

    public ValueReader getReader() { return _valueReader; }
    public String getName() { return _name; }

    public void setValueFor(Object bean, Object value) throws IOException
    {
        try {
            if (_setter == null) {
                _field.set(bean, value);
            } else {
                _setter.invoke(bean, value);
            }
        } catch (Exception e) {
            Throwable t = e;
            if (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
            throw new JSONObjectException("Failed to set property '"+_name+"'; exception "+e.getClass().getName()+"): "
                    +t.getMessage(), t);
        }
    }

    protected String _bean() {
        if (_setter != null) {
            return _setter.getDeclaringClass().getName();
        }
        return _field.getDeclaringClass().getName();
    }

    @Override
    public String toString() {
        return _name;
    }
}
