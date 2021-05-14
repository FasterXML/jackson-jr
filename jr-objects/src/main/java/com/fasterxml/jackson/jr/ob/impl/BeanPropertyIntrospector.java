package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition.Prop;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition.PropBuilder;

/**
 * Helper class that jackson-jr uses by default to introspect POJO properties
 * (represented as {@link POJODefinition}) to build general POJO readers
 * (deserializers) and writers (serializers).
 *<p>
 * Note that most of the usage is via {@link ValueReaderLocator} and
 * {@link ValueWriterLocator}
 *
 * @since 2.11
 */
public class BeanPropertyIntrospector
{
    protected final static Prop[] NO_PROPS = new Prop[0];

    private final static BeanPropertyIntrospector INSTANCE = new BeanPropertyIntrospector();

    public BeanPropertyIntrospector() { }

    public static BeanPropertyIntrospector instance() { return INSTANCE; }

    public POJODefinition pojoDefinitionForDeserialization(JSONReader r, Class<?> pojoType) {
        return _construct(pojoType, r.features());
    }

    public POJODefinition pojoDefinitionForSerialization(JSONWriter w, Class<?> pojoType) {
        return _construct(pojoType, w.features());
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private POJODefinition _construct(Class<?> beanType, int features)
    {
        Map<String,PropBuilder> propsByName = new TreeMap<String,PropBuilder>();
        _introspect(beanType, propsByName, features);

        Constructor<?> defaultCtor = null;
        Constructor<?> stringCtor = null;
        Constructor<?> longCtor = null;

        for (Constructor<?> ctor : beanType.getDeclaredConstructors()) {
            Class<?>[] argTypes = ctor.getParameterTypes();
            if (argTypes.length == 0) {
                defaultCtor = ctor;
            } else if (argTypes.length == 1) {
                Class<?> argType = argTypes[0];
                if (argType == String.class) {
                    stringCtor = ctor;
                } else if (argType == Long.class || argType == Long.TYPE) {
                    longCtor = ctor;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }
        final int len = propsByName.size();
        Prop[] props;
        if (len == 0) {
            props = NO_PROPS;
        } else {
            props = new Prop[len];
            int i = 0;
            for (PropBuilder builder : propsByName.values()) {
                props[i++] = builder.build();
            }
        }
        return new POJODefinition(beanType, props, defaultCtor, stringCtor, longCtor);
    }

    private static void _introspect(Class<?> currType, Map<String, PropBuilder> props,
            int features)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props, features);

        final boolean noStatics = JSON.Feature.INCLUDE_STATIC_FIELDS.isDisabled(features);
        // then public fields (since 2.8); may or may not be ultimately included
        // but at this point still possible
        for (Field f : currType.getDeclaredFields()) {
            if (!Modifier.isPublic(f.getModifiers())
                    || f.isEnumConstant() || f.isSynthetic()) {
                continue;
            }
            // Only include static members if (a) inclusion feature enabled and
            // (b) not final (cannot deserialize final fields)
            if (Modifier.isStatic(f.getModifiers())
                    && (noStatics || Modifier.isFinal(f.getModifiers()))) {
                continue;
            }
            _propFrom(props, f.getName()).withField(f);
        }

        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            // 13-Jun-2015, tatu: Skip synthetic, bridge methods altogether, for now
            //    at least (add more complex handling only if absolutely necessary)
            if (Modifier.isStatic(flags)
                    || m.isSynthetic() || m.isBridge()) {
                continue;
            }
            Class<?> argTypes[] = m.getParameterTypes();
            if (argTypes.length == 0) { // getter?
                // getters must be public to be used
                if (!Modifier.isPublic(flags)) {
                    continue;
                }

                Class<?> resultType = m.getReturnType();
                if (resultType == Void.class) {
                    continue;
                }
                String name = m.getName();
                if (name.startsWith("get")) {
                    if (name.length() > 3) {
                        name = decap(name.substring(3));
                        _propFrom(props, name).withGetter(m);
                    }
                } else if (name.startsWith("is")) {
                    if (name.length() > 2) {
                        // May or may not be used, but collect for now all the same:
                        name = decap(name.substring(2));
                        _propFrom(props, name).withIsGetter(m);
                    }
                }
            } else if (argTypes.length == 1) { // setter?
                // Non-public setters are fine if we can force access, don't yet check
                // let's also not bother about return type; setters that return value are fine
                String name = m.getName();
                if (!name.startsWith("set") || name.length() == 3) {
                    continue;
                }
                name = decap(name.substring(3));
                _propFrom(props, name).withSetter(m);
            }
        }
    }

    private static PropBuilder _propFrom(Map<String,PropBuilder> props, String name) {
        PropBuilder prop = props.get(name);
        if (prop == null) {
            prop = Prop.builder(name);
            props.put(name, prop);
        }
        return prop;
    }

    private static String decap(String name) {
        char c = name.charAt(0);
        char lowerC = Character.toLowerCase(c);

        if (c != lowerC) {
            // First: do NOT lower case if more than one leading upper case letters:
            if ((name.length() == 1)
                    || !Character.isUpperCase(name.charAt(1))) {
                char chars[] = name.toCharArray();
                chars[0] = lowerC;
                return new String(chars);
            }
        }
        return name;
    }

}
