package com.fasterxml.jackson.jr.ob.impl;

import com.fasterxml.jackson.jr.ob.impl.POJODefinition.Prop;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition.PropBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.fasterxml.jackson.jr.ob.JSON.Feature.INCLUDE_STATIC_FIELDS;
import static com.fasterxml.jackson.jr.ob.JSON.Feature.USE_FIELD_MATCHING_GETTERS;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.lang.reflect.Modifier.*;

/**
 * Helper class that jackson-jr uses by default to introspect POJO properties
 * (represented as {@link POJODefinition}) to build general POJO readers
 * (deserializers) and writers (serializers).
 * <p>
 * Note that most of the usage is via {@link ValueReaderLocator} and
 * {@link ValueWriterLocator}
 *
 * @since 2.11
 */
public final class BeanPropertyIntrospector {
    private BeanPropertyIntrospector() {
    }

    public static POJODefinition pojoDefinitionForDeserialization(JSONReader r, Class<?> pojoType) {
        return introspectDefinition(pojoType, false, r.features());
    }

    public static POJODefinition pojoDefinitionForSerialization(JSONWriter w, Class<?> pojoType) {
        return introspectDefinition(pojoType, true, w.features());
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    /**
     * Brain Method of {@link BeanPropertyIntrospector}, used to get the list of props
     */
    private static void _introspect(Class<?> currType, Map<String, PropBuilder> props, int features) {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props, features);
        final boolean isFieldNameGettersEnabled = USE_FIELD_MATCHING_GETTERS.isEnabled(features);
        final Map<String, Field> fieldNameMap = isFieldNameGettersEnabled ? new HashMap<>() : null;
        _populatePropsWithField(fieldNameMap, currType, props, features);
        _populatePropWithGettersAndSetters(isFieldNameGettersEnabled, fieldNameMap, currType, props);
    }

    private static void _populatePropWithGettersAndSetters(boolean isFieldNameGettersEnabled, Map<String, Field> fieldMap, Class<?> currType, Map<String, PropBuilder> props) {
        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            final Class<?> returnType = m.getReturnType();

            // 13-Jun-2015, tatu:
            //      Skip synthetic, bridge methods altogether, for now
            //      at least (add more complex handling only if absolutely necessary)
            if (isStatic(flags) || m.isSynthetic() || m.isBridge() || !isPublic(flags) || isGroovyMetaClass(returnType)) {
                continue;
            }

            final Class<?>[] argTypes = m.getParameterTypes();
            if (argTypes.length == 0 && returnType != Void.class) { // getter?
                generatePropsWithGetter(m, props);
                generatePropsWithIsGetter(m, props);
                generatePropsWithFieldMatchingGetter(isFieldNameGettersEnabled, fieldMap, m, props);
            } else if (argTypes.length == 1) { // setter?
                // Non-public setters are fine if we can force access, don't yet check
                // let's also not bother about return type; setters that return value are fine
                generatePropsWithSetter(m, props);
            }
        }
    }

    private static void _populatePropsWithField(Map<String, Field> fieldNameMap, Class<?> currType, Map<String, PropBuilder> props, int features) {
        // then public fields (since 2.8); may or may not be ultimately included but at this point still possible
        // Also, only include static members if
        //      (a) inclusion feature enabled and
        //      (b) not final (cannot deserialize final fields)
        for (Field f : currType.getDeclaredFields()) {
            if (fieldNameMap != null) {
                fieldNameMap.put(f.getName(), f);
            }
            if (!isPublic(f.getModifiers()) || f.isEnumConstant() || f.isSynthetic() || (isStatic(f.getModifiers()) && (INCLUDE_STATIC_FIELDS.isDisabled(features) || isFinal(f.getModifiers())))) {
                continue;
            }
            propFrom(props, f.getName()).withField(f);
        }
    }

    private static PropBuilder propFrom(Map<String, PropBuilder> props, String name) {
        return props.computeIfAbsent(name, Prop::builder);
    }

    private static String _decap(String name) {
        if (!isLowerCase(name.charAt(0)) && ((name.length() == 1) || !Character.isUpperCase(name.charAt(1)))) {
            final char[] chars = name.toCharArray();
            chars[0] = toLowerCase(name.charAt(0));
            return new String(chars);
        }
        return name;
    }

    /**
     * Helper method to detect Groovy's problematic metadata accessor type.
     *
     * @implNote Groovy MetaClass have cyclic reference, and hence the class containing it should not be serialised without
     * either removing that reference, or skipping over such references.
     */
    private static boolean isGroovyMetaClass(Class<?> clazz) {
        return "groovy.lang.MetaClass".equals(clazz.getName());
    }

    private static void generatePropsWithFieldMatchingGetter(boolean isFieldNameGettersEnabled,Map<String, Field> fieldNameMap, final Method m, final Map<String, PropBuilder> props) {
        final String name = m.getName();
        if (isFieldNameGettersEnabled) {
            // 10-Mar-2024: [jackson-jr#94]:
            //  This will allow getters with field name as their getters,
            // like the ones generated by Groovy (or JDK 17 for Records).
            // If method name matches with field name, & method return
            // type matches the field type only then it can be considered a getter.
            Field field = fieldNameMap.get(name);
            if (field != null && Modifier.isPublic(m.getModifiers()) && m.getReturnType().equals(field.getType())) {
                // NOTE: do NOT decap, field name should be used as-is
                propFrom(props, name).withGetter(m);
            }
        }
    }

    private static void generatePropsWithGetter(final Method method, final Map<String, PropBuilder> props) {
        final String getterPrefix = "get";
        final String name = method.getName();
        if (name.startsWith(getterPrefix) && name.length() > getterPrefix.length()) {
            propFrom(props, _decap(name.substring(getterPrefix.length()))).withGetter(method);
        }
    }

    private static void generatePropsWithIsGetter(final Method method, final Map<String, PropBuilder> props) {
        final String isGetterPrefix = "is";
        final String name = method.getName();
        if (name.startsWith(isGetterPrefix) && name.length() > isGetterPrefix.length()) {
            propFrom(props, _decap(name.substring(isGetterPrefix.length()))).withIsGetter(method);
        }
    }

    private static void generatePropsWithSetter(final Method method, final Map<String, PropBuilder> props) {
        final String setterPrefix = "set";
        final String name = method.getName();
        if (name.startsWith(setterPrefix) && name.length() > setterPrefix.length()) {
            propFrom(props, _decap(name.substring(setterPrefix.length()))).withSetter(method);
        }
    }

    private static POJODefinition introspectDefinition(Class<?> beanType, boolean forSerialization, int features) {
        final Map<String, PropBuilder> propsByName = new TreeMap<>();
        _introspect(beanType, propsByName, features);

        final BeanConstructors constructors = new BeanConstructors(beanType);
        for (Constructor<?> ctor : beanType.getDeclaredConstructors()) {
            final Class<?>[] argTypes = ctor.getParameterTypes();
            if (argTypes.length == 0) {
                constructors.addNoArgsConstructor(ctor);
            } else if (argTypes.length == 1) {
                final Class<?> argType = argTypes[0];
                if (argType == String.class) {
                    constructors.addStringConstructor(ctor);
                } else if (argType == Integer.class || argType == Integer.TYPE) {
                    constructors.addIntConstructor(ctor);
                } else if (argType == Long.class || argType == Long.TYPE) {
                    constructors.addLongConstructor(ctor);
                }
            }
        }

        final Prop[] props = propsByName.values().stream().map(PropBuilder::build).toArray(Prop[]::new);
        return new POJODefinition(beanType, props, forSerialization ? null : constructors);
    }
}