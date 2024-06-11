package com.fasterxml.jackson.jr.ob.impl;

import com.fasterxml.jackson.jr.ob.impl.POJODefinition.PropBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Helper class to get Java Record metadata.
 */
public final class RecordsHelpers {
    private static boolean supportsRecords;

    private static Method isRecordMethod;
    private static Method getRecordComponentsMethod;
    private static Method getTypeMethod;

    static {
        Method isRecordMethod;
        Method getRecordComponentsMethod;
        Method getTypeMethod;

        try {
            isRecordMethod = Class.class.getMethod("isRecord");
            getRecordComponentsMethod = Class.class.getMethod("getRecordComponents");
            Class<?> recordComponentClass = Class.forName("java.lang.reflect.RecordComponent");
            getTypeMethod = recordComponentClass.getMethod("getType");
            supportsRecords = true;
        } catch (Throwable t) {
            isRecordMethod = null;
            getRecordComponentsMethod = null;
            getTypeMethod = null;
            supportsRecords = false;
        }

        RecordsHelpers.isRecordMethod = isRecordMethod;
        RecordsHelpers.getRecordComponentsMethod = getRecordComponentsMethod;
        RecordsHelpers.getTypeMethod = getTypeMethod;
    }
    private RecordsHelpers() {}

    static boolean isRecordConstructor(Class<?> beanClass, Constructor<?> ctor, Map<String, PropBuilder> propsByName) {
        if (!supportsRecords || !isRecordType(beanClass)) {
            return false;
        }

        Class<?>[] parameterTypes = ctor.getParameterTypes();
        if (parameterTypes.length != propsByName.size()) {
            return false;
        }

        try {
            Object[] recordComponents = (Object[]) getRecordComponentsMethod.invoke(beanClass);
            Class<?>[] componentTypes = new Class<?>[recordComponents.length];
            for (int i = 0; i < recordComponents.length; i++) {
                Object recordComponent = recordComponents[i];
                Class<?> type = (Class<?>) getTypeMethod.invoke(recordComponent);
                componentTypes[i] = type;
            }

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] != componentTypes[i]) {
                    return false;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
        return true;
    }

    static boolean isRecordType(Class<?> cls) {
        Class<?> parent = cls.getSuperclass();
        return (parent != null) && "java.lang.Record".equals(parent.getName());
    }
}
