package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Field;
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

    public BeanProperty(String name) {
        _name = new SerializedString(name);
        _typeId = 0;
        _field = null;
        _getMethod = null;
        _setMethod = null;
        _valueReader = null;
    }

    protected BeanProperty(BeanProperty src, ValueReader vr) {
        _name = src._name;
        _typeId = src._typeId;
        _field = src._field;
        _getMethod = src._getMethod;
        _setMethod = src._setMethod;
        _valueReader = vr;
    }

    @Deprecated // since 2.8
    protected BeanProperty(BeanProperty src, int typeId, Method getter, Method setter) {
        this(src, typeId, getter, setter, null);
    }

    protected BeanProperty(BeanProperty src, int typeId,
            Method getter, Method setter, Field field)
    {
        _name = src._name;
        _valueReader = src._valueReader;

        _typeId = typeId;
        _field = field;
        _getMethod = getter;
        _setMethod = setter;
    }
    
    public BeanProperty withGetter(Method getter) {
        return new BeanProperty(this, _typeId, getter, _setMethod, _field);
    }

    public BeanProperty withSetter(Method setter) {
        /* 28-Jul-2014, tatu: Need to figure out a way to resolve multiple similarly
         *    named setters, mostly to avoid bridge methods (see [jackson-jr#15]),
         *    but possible also to try to find most optimal of overloads.
         */
        if (_setMethod != null) {
            // start with minimal conflict resolution, however
            if (setter.isBridge() || setter.isSynthetic()) {
                return this;
            }
        }
        return new BeanProperty(this, _typeId, _getMethod, setter, _field);
    }

    public BeanProperty withReader(ValueReader vr) {
        return new BeanProperty(this, vr);
    }

    public BeanProperty withTypeId(int typeId) {
        return (typeId == _typeId) ? this
                : new BeanProperty(this, typeId, _getMethod, _setMethod, _field);
    }

    public void forceAccess() {
        if (_getMethod != null) {
            _getMethod.setAccessible(true);
        }
        if (_setMethod != null) {
            _setMethod.setAccessible(true);
        }
        
    }

    public boolean hasGetter() { return _getMethod != null; }
    public boolean hasSetter() { return _setMethod != null; }
    public boolean hasField() { return _field != null; }
    
    public Type genericSetterType() {
        return _setMethod.getGenericParameterTypes()[0];
    }

    public Class<?> rawSetterType() {
        return _setMethod.getParameterTypes()[0];
    }

    public Class<?> rawGetterType() {
        return _getMethod.getReturnType();
    }
    
    public ValueReader getReader() { return _valueReader; }
    
    public int getTypeId() { return _typeId; }

    public SerializedString getName() { return _name; }
    
    public SerializedString getNameIfHasSetter() {
        return (_setMethod == null) ? null : _name;
    }
    
    public Object getValueFor(Object bean) throws IOException
    {
        if (_getMethod == null) {
            throw new IllegalStateException("No getter for property '"+_name+"' (type "+_bean()+")");
        }
        try {
            return _getMethod.invoke(bean);
        } catch (Exception e) {
            throw new JSONObjectException("Failed to access property '"+_name+"'; exception "+e.getClass().getName()+"): "
                    +e.getMessage(), e);
        }
    }

    public Object setValueFor(Object bean, Object value) throws IOException
    {
        if (_setMethod == null) {
            throw new IllegalStateException("No setter for property '"+_name+"' (type "+_bean()+")");
        }
        try {
            return _setMethod.invoke(bean, value);
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            throw new JSONObjectException("Failed to set property '"+_name+"'; exception "+e.getClass().getName()+"): "
                    +e.getMessage(), e);
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
