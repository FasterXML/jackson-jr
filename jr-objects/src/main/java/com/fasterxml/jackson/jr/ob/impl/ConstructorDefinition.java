package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.function.Supplier;

public class ConstructorDefinition {
    private final HashMap<Class<?>, Constructor<?>> constructorMap = new HashMap<>();

    public ConstructorDefinition register(Class<?> firstArgClass, Constructor<?> constructor) {
        constructorMap.putIfAbsent(firstArgClass, constructor);
        return this;
    }

    public <T> Constructor<T> getConstructor(Class<T> firstArgClass) {
        return (Constructor<T>) constructorMap.get(firstArgClass);
    }

    public <X extends Throwable, T> T generateOrThrow(Class<T> firstArgClass, Object initargs, Supplier<? extends X> exceptionSupplier)
            throws X,Exception {
        if (constructorMap.containsKey(firstArgClass)) return getConstructor(firstArgClass).newInstance(initargs);
        else throw exceptionSupplier.get();
    }

    public Object generateDefault(Class<?> valueType) throws Exception {
        if (constructorMap.containsKey(null)) return constructorMap.get(null).newInstance((Object[]) null);
        else throw new IllegalStateException("Class " + valueType.getName() + " does not have default constructor to use");
    }
}