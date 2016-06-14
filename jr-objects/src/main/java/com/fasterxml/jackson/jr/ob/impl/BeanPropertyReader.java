package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.fasterxml.jackson.jr.ob.JSONObjectException;

public final class BeanPropertyReader
{
    protected final String _name;

    /**
     * For non-trivial non-bean types
     */
    protected final ValueReader _valueReader;

    protected final Method _setMethod;

    /**
     * @since 2.8
     */
    protected final Field _field;

    public BeanPropertyReader(String name, Field f, Method setter) {
        _name = name;
        _field = f;
        _setMethod = setter;
        _valueReader = null;
    }

    protected BeanPropertyReader(BeanPropertyReader src, ValueReader vr) {
        _name = src._name;
        _field = src._field;
        _setMethod = src._setMethod;
        _valueReader = vr;
    }

    public BeanPropertyReader withReader(ValueReader vr) {
        return new BeanPropertyReader(this, vr);
    }

    public Type genericSetterType() {
        return _setMethod.getGenericParameterTypes()[0];
    }

    public Class<?> rawSetterType() {
        return _setMethod.getParameterTypes()[0];
    }

    public ValueReader getReader() { return _valueReader; }
    public String getName() { return _name; }

    public Object setValueFor(Object bean, Object value) throws IOException
    {
        if (_setMethod == null) {
            throw new IllegalStateException("No setter for property '"+_name+"' (type "+_bean()+")");
        }
        try {
            return _setMethod.invoke(bean, value);
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
        if (_setMethod != null) {
            return _setMethod.getDeclaringClass().getName();
        }
        return _field.getDeclaringClass().getName();
    }

    @Override
    public String toString() {
        return _name;
    }
}
