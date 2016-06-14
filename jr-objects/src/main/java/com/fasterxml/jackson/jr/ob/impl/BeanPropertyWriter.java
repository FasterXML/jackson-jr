package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Simple container class used to contain information needed for serializing
 * value of a single POJO property.
 *
 * @since 2.8
 */
public final class BeanPropertyWriter
{
    public final SerializedString name;
    public final int typeId;

    protected final Method _getter;

    protected final Field _field;

    public BeanPropertyWriter(int typeId, String n, Field f, Method getter)
    {
        this.typeId = typeId;
        name = new SerializedString(n);
        _field = f;
        if ((getter == null) && (f == null)) {
            throw new IllegalArgumentException("Missing getter and field");
        }
        _getter = getter;
    }

    public Object getValueFor(Object bean) throws IOException
    {
        try {
            if (_getter == null) {
                return _field.get(bean);
            }
            return _getter.invoke(bean);
        } catch (Exception e) {
            throw new JSONObjectException(String.format(
                    "Failed to access property '%s'; exception (%s): %s",
                    name, e.getClass().getName(), e.getMessage()), e);
        }
    }

    protected String _bean() {
        if (_getter == null) {
            return _field.getDeclaringClass().getName();
        }
        return _getter.getDeclaringClass().getName();
    }
}
