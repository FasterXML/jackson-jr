package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;

/**
 * Container class added in 2.17 to encapsulate collection and use
 * of various Constructors for User-defined (non-JDK) types (aka
 * "Beans").
 */
public class BeanConstructors
{
    protected final Class<?> _valueType;

    protected Constructor<?> _noArgsCtor;
    protected Constructor<?> _stringCtor;
    protected Constructor<?> _longCtor;

    public BeanConstructors(Class<?> valueType) {
        _valueType = valueType;
    }

    public BeanConstructors addNoArgsConstructor(Constructor<?> ctor) {
        _noArgsCtor = ctor;
        return this;
    }

    public BeanConstructors addStringConstructor(Constructor<?> ctor) {
        _stringCtor = ctor;
        return this;
    }

    public BeanConstructors addLongConstructor(Constructor<?> ctor) {
        _longCtor = ctor;
        return this;
    }

    public void forceAccess() {
        if (_noArgsCtor != null) {
            _noArgsCtor.setAccessible(true);
        }
        if (_stringCtor != null) {
            _stringCtor.setAccessible(true);
        }
        if (_longCtor != null) {
            _longCtor.setAccessible(true);
        }
    }

    protected Object create() throws Exception {
        if (_noArgsCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have default constructor to use");
        }
        return _noArgsCtor.newInstance((Object[]) null);
    }
    
    protected Object create(String str) throws Exception {
        if (_stringCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have single-String constructor to use");
        }
        return _stringCtor.newInstance(str);
    }

    protected Object create(long l) throws Exception {
        if (_longCtor == null) {
            throw new IllegalStateException("Class "+_valueType.getName()+" does not have single-long constructor to use");
        }
        return _longCtor.newInstance(l);
    }
}
