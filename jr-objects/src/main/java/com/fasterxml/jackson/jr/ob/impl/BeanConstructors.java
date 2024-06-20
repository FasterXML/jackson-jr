package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;

/**
 * Container class added to encapsulate details of collection and use of
 * Constructors for User-defined (non-JDK) types (aka "Beans").
 * 
 * @since 2.17
 */
public class BeanConstructors
{
    protected final Class<?> _valueType;

    protected Constructor<?> _noArgsCtor;

    protected Constructor<?> _intCtor;
    protected Constructor<?> _longCtor;
    protected Constructor<?> _stringCtor;

    public BeanConstructors(Class<?> valueType) {
        _valueType = valueType;
    }

    public BeanConstructors addNoArgsConstructor(Constructor<?> ctor) {
        _noArgsCtor = ctor;
        return this;
    }

    public BeanConstructors addIntConstructor(Constructor<?> ctor) {
        _intCtor = ctor;
        return this;
    }

    public BeanConstructors addLongConstructor(Constructor<?> ctor) {
        _longCtor = ctor;
        return this;
    }

    public BeanConstructors addStringConstructor(Constructor<?> ctor) {
        _stringCtor = ctor;
        return this;
    }

    public void forceAccess() {
        if (_noArgsCtor != null) {
            _noArgsCtor.setAccessible(true);
        }
        if (_intCtor != null) {
            _intCtor.setAccessible(true);
        }
        if (_longCtor != null) {
            _longCtor.setAccessible(true);
        }
        if (_stringCtor != null) {
            _stringCtor.setAccessible(true);
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
        // 23-Feb-2024, tatu: As per [jackson-jr#25] can have `int`-constructors too.
        //    For now no need to try to optimize separately
        if (_longCtor != null) {
            return _longCtor.newInstance(l);
        }
        if (_intCtor != null) {
            // TODO: should this do bounds checks?
            return _intCtor.newInstance((int) l);
        }
        throw new IllegalStateException("Class "+_valueType.getName()
            +" does not have single-long or single-int constructor to use");
    }
}
