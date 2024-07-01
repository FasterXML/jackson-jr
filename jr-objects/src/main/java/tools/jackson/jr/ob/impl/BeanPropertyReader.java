package tools.jackson.jr.ob.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import tools.jackson.core.JacksonException;
import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.ValueReader;

public final class BeanPropertyReader
{
    /**
     * Logical name of the property being handled.
     */
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
     *
     * @since 2.18
     */
    private final int _index;

    /**
     * @since 2.18
     */
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

    @Deprecated // @since 2.18
    public BeanPropertyReader(String name, Field f, Method setter) {
        this(name, f, setter, -1);
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

    public void setValueFor(Object bean, Object[] valueBuf) throws JacksonException
    {
        if (_setter == null) {
            try {
                _field.set(bean, valueBuf[0]);
            } catch (Exception e) {
                _reportProblem(e);
            }
            return;
        }
        try {
            _setter.invoke(bean, valueBuf);
        } catch (Exception e) {
            _reportProblem(e);
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

    private void _reportProblem(Exception e) throws JacksonException
    {
        Throwable t = e;
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
        }
        throw new JSONObjectException("Failed to set property '"+_name+"'; exception "+e.getClass().getName()+"): "
                +t.getMessage(), t);
    }
}
