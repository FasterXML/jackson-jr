package com.fasterxml.jackson.jr.ob.impl;

/**
 * Utilities to help with reflection and types
 */
public final class Types {
    private Types() {
        // utility class
    }

    /**
     * Detect whether a class is an enum or a subclass of an enum - e.g. an anonymous inner class
     * inside an enum
     * @param type the type to inspect
     * @return <code>true</code> if effectively an enum
     */
    public static boolean isEnum(Class<?> type) {
        return type.isEnum() || isParentEnum(type);
    }

    private static boolean isParentEnum(Class<?> type) {
        Class<?> superClass = type.getSuperclass();
        return superClass != null && isEnum(superClass);
    }
}
