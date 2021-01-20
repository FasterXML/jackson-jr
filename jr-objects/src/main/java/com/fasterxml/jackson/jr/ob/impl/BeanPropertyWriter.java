package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Simple container class used to contain information needed for serializing
 * value of a single POJO property.
 */
public final class BeanPropertyWriter
{
    public final SerializedString name;
    public final int typeId;

    private final Field _field;
    private final Method _getter;

    public BeanPropertyWriter(int typeId, String n, Field f, Method getter)
    {
        this.typeId = typeId;
        name = new SerializedString(n);
        if ((getter == null) && (f == null)) {
            throw new IllegalArgumentException("Missing getter and field");
        }
        _field = f;
        _getter = getter;
    }

    public BeanPropertyWriter withName(String newName) {
        if (name.toString().equals(newName)) {
            return this;
        }
        return new BeanPropertyWriter(typeId, newName, _field, _getter);
    }

    public Object getValueFor(Object bean) throws JacksonException
    {
        try {
            if (_getter == null) {
                return _field.get(bean);
            }
            return _getter.invoke(bean);
        } catch (Exception e) {
            final String accessorDesc = (_getter != null)
                    ? String.format("method %s.%s()", _bean(), _getter.getName())
                    : String.format("field %s.%s", _bean(), _field.getName());
            throw new JSONObjectException(String.format(
                    "Failed to access property '%s' (using %s); exception (%s): %s",
                    name, e.getClass().getName(), accessorDesc, e.getMessage()), e);
        }
    }

    protected String _bean() {
        if (_getter == null) {
            return _field.getDeclaringClass().getName();
        }
        return _getter.getDeclaringClass().getName();
    }
}
