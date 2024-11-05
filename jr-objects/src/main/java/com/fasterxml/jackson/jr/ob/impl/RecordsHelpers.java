package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.jr.ob.impl.POJODefinition.PropBuilder;

/**
 * Helper class to get Java Record metadata.
 *
 * @since 2.18
 */
public final class RecordsHelpers {
    private static boolean supportsRecords;

    private static Method getRecordComponentsMethod;
    private static Method getComponentTypeMethod;

    // We may need this in future:
    //private static Method getComponentNameMethod;

    static {
        try {
            getRecordComponentsMethod = Class.class.getMethod("getRecordComponents");
            Class<?> recordComponentClass = Class.forName("java.lang.reflect.RecordComponent");
            getComponentTypeMethod = recordComponentClass.getMethod("getType");
            //getComponentNameMethod = recordComponentClass.getMethod("getName");
            supportsRecords = true;
        } catch (Throwable t) {
            supportsRecords = false;
        }
    }
    private RecordsHelpers() {}

    static Constructor<?> findCanonicalConstructor(Class<?> beanClass) {
        // sanity check: caller shouldn't rely on it
        if (!supportsRecords || !isRecordType(beanClass)) {
            return null;
        }
        try {
            final Class<?>[] componentTypes = componentTypes(beanClass);
            for (Constructor<?> ctor : beanClass.getDeclaredConstructors()) {
                final Class<?>[] parameterTypes = ctor.getParameterTypes();
                if (parameterTypes.length == componentTypes.length) {
                    if (Arrays.equals(parameterTypes, componentTypes)) {
                        return ctor;
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            ;
        }
        return null;
    }

    static boolean isRecordConstructor(Class<?> beanClass, Constructor<?> ctor,
            Map<String, PropBuilder> propsByName)
    {
        if (!supportsRecords || !isRecordType(beanClass)) {
            return false;
        }

        Class<?>[] parameterTypes = ctor.getParameterTypes();
        if (parameterTypes.length != propsByName.size()) {
            return false;
        }

        try {
            Class<?>[] componentTypes = componentTypes(beanClass);
            return Arrays.equals(parameterTypes, componentTypes);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    static boolean isRecordType(Class<?> cls) {
        Class<?> parent = cls.getSuperclass();
        return (parent != null) && "java.lang.Record".equals(parent.getName());
    }

    static List<String> recordPropertyNames(Class<?> cls) {
        // Let's base this on public fields
        return Arrays.asList(cls.getDeclaredFields()).stream().map(Field::getName)
                .collect(Collectors.toList());
    }

    private static Class<?>[] componentTypes(Class<?> recordType)
        throws ReflectiveOperationException
    {
        Object[] recordComponents = (Object[]) getRecordComponentsMethod.invoke(recordType);
        Class<?>[] componentTypes = new Class<?>[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            Object recordComponent = recordComponents[i];
            Class<?> type = (Class<?>) getComponentTypeMethod.invoke(recordComponent);
            componentTypes[i] = type;
        }
        return componentTypes;
    }
}
