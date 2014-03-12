package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class BeanProperty
{
    protected final SerializedString _name;
    
    protected final Class<?> _rawType;

    /**
     * Pre-resolved type id for writing values, if statically known.
     */
    protected final int _writeTypeId;
    
    protected final Method _getMethod, _setMethod;

    public BeanProperty(String name, Class<?> rawType,
            Method readMethod, Method writeMethod)
    {
        this(new SerializedString(name), rawType, readMethod, writeMethod,
                0);
    }

    protected BeanProperty(SerializedString name, Class<?> rawType,
            Method getMethod, Method setMethod,
            int writeTypeId)
    {
        _name = name;
        _rawType = rawType;
        _getMethod = getMethod;
        _setMethod = setMethod;
        _writeTypeId = writeTypeId;
    }

    public BeanProperty withWriteTypeId(int id) {
        if (_writeTypeId == id) {
            return this;
        }
        return new BeanProperty(_name, _rawType, _getMethod, _setMethod, id);
    }

    public final int getWriteTypeId() { return _writeTypeId; }

    public SerializedString getName() { return _name; }
    
    public SerializedString getNameIfHasSetter() {
        return (_setMethod == null) ? null : _name;
    }
    
    public Object getValueFor(Object bean) throws IOException
    {
        try {
            return _getMethod.invoke(bean);
        } catch (Exception e) {
            throw new JSONObjectException("Failed to access property '"+_name+"' (type "
                    +_rawType.getName()+"; exception "+e.getClass().getName()+"): "
                    +e.getMessage(), e);
        }
    }
}
