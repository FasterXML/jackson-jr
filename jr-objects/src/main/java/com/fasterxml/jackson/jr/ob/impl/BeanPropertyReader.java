package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * @since 2.8 (formerly part of `BeanDefinition`)
 */
public final class BeanPropertyReader
{
    private final String _name;

    /**
     * For non-trivial non-bean types
     */
    private final ValueReader _valueReader;

    /**
     * Setter method to use, if any; null if none
     */
    private final Method _setter;

    /**
     * Field to assign value to, if no setter method defined; null if none
     */
    private final Field _field;

    /**
     * Index used for {@code Record}s constructor parameters. It is not used for getter/setter methods.
     */
    private final int _index;

    public BeanPropertyReader(String name, Field f, Method setter, int propertyIndex) {
        if ((f == null) && (setter == null)) {
            throw new IllegalArgumentException("Both `field` and `setter` can not be null");
        }
        _name = name;
        _field = f;
        _setter = setter;
        _valueReader = null;
        _index = propertyIndex;
    }

    protected BeanPropertyReader(BeanPropertyReader src, ValueReader vr) {
        _name = src._name;
        _field = src._field;
        _setter = src._setter;
        _index = src._index;
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

    public int getIndex() {
        return _index;
    }

    public void setValueFor(Object bean, Object[] valueBuf)
        throws IOException
    {
        try {
            if (_setter == null) {
                _field.set(bean, valueBuf[0]);
            } else {
                _setter.invoke(bean, valueBuf);
            }
        } catch (Exception e) {
            Throwable t = e;
            if (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
            final Object value = valueBuf[0];
            final String valueTypeDesc = (value == null) ? "NULL" : value.getClass().getName();
            throw new JSONObjectException(String.format(
                    "Failed to set property '%s' (raw type %s) to value of type %s; exception (%s): %s",
                _name, _rawType().getName(), valueTypeDesc, e.getClass().getName(), t.getMessage()),
                t);
        }
    }

    protected String _bean() {
        if (_setter != null) {
            return _setter.getDeclaringClass().getName();
        }
        return _field.getDeclaringClass().getName();
    }

    protected Class<?> _rawType() {
        if (_setter != null) {
            return _setter.getParameterTypes()[0];
        }
        return _field.getType();
    }

    @Override
    public String toString() {
        return _name;
    }
}
