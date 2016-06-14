package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public final class BeanProperty
{
    protected final SerializedString _name;

    /**
     * Pre-resolved type id for reading/writing values, if statically known.
     *<p>
     * Note: yes, access is without either volatile or synchronized. But it is
     * an atomic type; so in the very worst case, modification just won't
     * stick. It will never result in invalid value being accessible.
     */
    protected int _typeId;

    /**
     * For non-trivial non-bean types
     */
    protected final ValueReader _valueReader;

    protected final Method _getMethod, _setMethod;

    /**
     * @since 2.8
     */
    protected final Field _field;

    protected final boolean _hasSetter;

    public BeanProperty(String name) {
        _name = new SerializedString(name);
        _typeId = 0;
        _field = null;
        _getMethod = null;
        _setMethod = null;
        _valueReader = null;
        _hasSetter = false;
    }

    private BeanProperty(String name, Method getter, Method setter, Field field,
            boolean hasSetter)
    {
        _name = new SerializedString(name);
        _typeId = 0;
        _field = field;
        _getMethod = getter;
        _setMethod = setter;
        _valueReader = null;
        _hasSetter = hasSetter;
    }
    
    protected BeanProperty(BeanProperty src, ValueReader vr) {
        _name = src._name;
        _typeId = src._typeId;
        _field = src._field;
        _getMethod = src._getMethod;
        _setMethod = src._setMethod;
        _valueReader = vr;
        _hasSetter = src._hasSetter;
    }

    protected BeanProperty(BeanProperty src, int typeId,
            Method getter, Method setter, Field field)
    {
        _name = src._name;
        _valueReader = src._valueReader;
        _hasSetter = src._hasSetter;

        _typeId = typeId;
        _field = field;
        _getMethod = getter;
        _setMethod = setter;
    }

    public static BeanProperty forDeserialization(String name, Method setter, Field field) {
        return new BeanProperty(name, null, setter, field, false);
    }

    public BeanProperty withReader(ValueReader vr) {
        return new BeanProperty(this, vr);
    }

    public BeanProperty withTypeId(int typeId) {
        return (typeId == _typeId) ? this
                : new BeanProperty(this, typeId, _getMethod, _setMethod, _field);
    }

    public Type genericSetterType() {
        return _setMethod.getGenericParameterTypes()[0];
    }

    public Class<?> rawSetterType() {
        return _setMethod.getParameterTypes()[0];
    }

    public ValueReader getReader() { return _valueReader; }
    
    public int getTypeId() { return _typeId; }
    public SerializedString getName() { return _name; }

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
        Class<?> cls;
        if (_getMethod != null) {
            cls = _getMethod.getDeclaringClass();
        } else if (_setMethod != null) {
            cls = _setMethod.getDeclaringClass();
        } else {
            return "UNKNOWN";
        }
        return cls.getName();
    }

    @Override
    public String toString() {
        return _name.toString();
    }
}
