package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Definition of a single Bean-style Java class, without assumptions
 * on usage for serialization or deserialization, used as input
 * for constructing bean readers and writers.
 *
 * @since 2.8
 */
public class POJODefinition
{
    /**
     * Since there is just one global cache for type lookups, let's
     * limit the size to something quite modest: introspection is
     * not free, but holding onto information can cause issues
     * for larger systems
     */
    private final static int MAX_CACHED = 100;

    private final static Prop[] NO_PROPS = new Prop[0];

    protected final static ConcurrentHashMap<Class<?>, POJODefinition> DEFS
        = new ConcurrentHashMap<Class<?>, POJODefinition>(50, 0.75f, 4);
    
    protected final Class<?> _type;

    protected final Prop[] _properties;

    public final Constructor<?> defaultCtor;
    public final Constructor<?> stringCtor;
    public final Constructor<?> longCtor;

    /**
     * Constructors used for serialization use case
     */
    public POJODefinition(Class<?> type, Prop[] props,
            Constructor<?> defaultCtor0, Constructor<?> stringCtor0, Constructor<?> longCtor0)
    {
        _type = type;
        _properties = props;
        defaultCtor = defaultCtor0;
        stringCtor = stringCtor0;
        longCtor = longCtor0;
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    public static POJODefinition find(Class<?> forType)
    {
        try {
            return _find(forType);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format
                    ("Failed to introspect ClassDefinition for type '%s': %s",
                            forType.getName(), e.getMessage()), e);
        }
    }
    
    public static POJODefinition _find(Class<?> forType) {
        POJODefinition def = DEFS.get(forType);
        if (def == null) {
            def = _construct(forType);
            // Instead of trying to do LRU or just (which gets tricky with concurrent
            // access), let's just do "flush when full". In practice tends to work
            // quite well.
            if (DEFS.size() >= MAX_CACHED) {
                DEFS.clear();
            }
            DEFS.putIfAbsent(forType, def);
        }
        return def;
    }

    public Prop[] properties() {
        return _properties;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private static POJODefinition _construct(Class<?> beanType)
    {
        Map<String,Prop> propsByName = new TreeMap<String,Prop>();
        _introspect(beanType, propsByName);

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
        Prop[] props;
        if (propsByName.isEmpty()) {
            props = NO_PROPS;
        } else {
            props = propsByName.values().toArray(NO_PROPS);
        }
        return new POJODefinition(beanType, props, defaultCtor, stringCtor, longCtor);
    }

    private static void _introspect(Class<?> currType, Map<String,Prop> props)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props);

        // then public fields (since 2.8); may or may not be ultimately included
        // but at this point still possible
        for (Field f : currType.getDeclaredFields()) {
            if (!Modifier.isPublic(f.getModifiers())
                    || f.isEnumConstant() || f.isSynthetic()) {
                continue;
            }
            _propFrom(props, f.getName()).field = f;
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
                        _propFrom(props, name).getter = m;
                    }
                } else if (name.startsWith("is")) {
                    if (name.length() > 2) {
                        // May or may not be used, but collect for now all the same:
                        name = decap(name.substring(2));
                        _propFrom(props, name).isGetter = m;
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
                _propFrom(props, name).setter = m;
            }
        }
    }

    private static Prop _propFrom(Map<String,Prop> props, String name) {
        Prop prop = props.get(name);
        if (prop == null) {
            prop = new Prop(name);
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

    /*
    /**********************************************************************
    /* Helper class for containing property definitions
    /**********************************************************************
     */

    public static final class Prop
    {
        public final String name;

        public Method setter, getter, isGetter;
        public Field field;
        
        public Prop(String name) {
            this.name = name;
        }

        public boolean hasSetter() {
            return (setter != null) || (field != null);
        }
    }
}
