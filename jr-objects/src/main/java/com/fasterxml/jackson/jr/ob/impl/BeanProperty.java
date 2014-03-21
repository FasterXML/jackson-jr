package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public final class BeanProperty
{
    protected final SerializedString _name;
    
    protected final Class<?> _rawType;

    /**
     * Pre-resolved type id for reading/writing values, if statically known.
     *<p>
     * Note: yes, access is without either volatile or synchronized. But it is
     * an atomic type; so in the very worst case, modification just won't
     * stick. It will never result in invalid value being accessible.
     */
    protected int _typeId;
    
    protected final Method _getMethod, _setMethod;

    public BeanProperty(String name, Class<?> rawType, int typeId,
            Method readMethod, Method writeMethod)
    {
        this(new SerializedString(name), rawType, typeId, readMethod, writeMethod);
    }

    protected BeanProperty(SerializedString name, Class<?> rawType, int typeId,
            Method getMethod, Method setMethod)
    {
        _name = name;
        _rawType = rawType;
        _getMethod = getMethod;
        _setMethod = setMethod;
        _typeId = typeId;
    }

    public void overridTypeId(int id) {
        _typeId = id;
    }

    public Class<?> getType() { return _rawType; }
    
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
            throw new JSONObjectException("Failed to access property '"+_name+"' (type "
                    +_rawType.getName()+"; exception "+e.getClass().getName()+"): "
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
            throw new JSONObjectException("Failed to set property '"+_name+"' (type "
                    +_rawType.getName()+"; exception "+e.getClass().getName()+"): "
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
}
