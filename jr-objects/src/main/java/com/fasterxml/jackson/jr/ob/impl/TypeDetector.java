package com.fasterxml.jackson.jr.ob.impl;

import java.beans.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.BeanDefinition;
import com.fasterxml.jackson.jr.ob.BeanProperty;
import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Helper object used for efficient detection of type information
 * relevant to our conversion needs.
 *<p>
 * Note that usage pattern is such that a single "root" instance is kept
 * by each {@link com.fasterxml.jackson.jr.ob.JSON} instance; and
 * an actual per-operation instance must be constructed by calling
 * {@link #perOperationInstance}: reason for this is that instances
 * use simple caching to handle the common case of repeating types
 * within JSON Arrays.
 */
public class TypeDetector
{
    /*
    /**********************************************************************
    /* Value constants for serialization
    /**********************************************************************
     */

    /* FAQ: Why ints? Why not Enums?!? Glad you asked: one reasons is class
     * size reduction: javac creates annonymous inner class for each switch
     * on enum (per referring type and enum: can combine multiple). So by
     * not using enunms we try to minimize code foot print.
     * But this is ONLY done because Value Type constants are NOT part of
     * public API: if they were, size savings wouldn't make sense.
     * 
     * One more note: negative values are used for dynamically introspected
     * Beans.
     */
    
    /**
     * Type not yet resolved
     */
    public final static int SER_UNKNOWN = 0;
    
    /**
     * All kinds of {@link java.util.Map}s.
     */
    public final static int SER_MAP = 1;

    /**
     * All kinds of {@link java.util.List}s.
     */
    public final static int SER_LIST = 2;

    /**
     * All kinds of {@link java.util.Collection}s other than {@link java.util.List}s
     */
    public final static int SER_COLLECTION = 3;

    /**
     * Arrays of non-primitive types
     */
    public final static int SER_OBJECT_ARRAY = 4;

    public final static int SER_INT_ARRAY = 5;
    public final static int SER_LONG_ARRAY = 6;
    public final static int SER_BOOLEAN_ARRAY = 7;
    
    /**
     * An implementation of {@link com.fasterxml.jackson.core.TreeNode}
     */
    public final static int SER_TREE_NODE = 8;
    
    // // // String(-like) types

    public final static int SER_STRING = 9;
    public final static int SER_CHARACTER_SEQUENCE = 10;
    public final static int SER_CHAR_ARRAY = 11;
    public final static int SER_BYTE_ARRAY = 12;

    // // // Numbers
    
    public final static int SER_NUMBER_BYTE = 13;

    public final static int SER_NUMBER_SHORT = 14;
    
    public final static int SER_NUMBER_INTEGER = 15;

    public final static int SER_NUMBER_LONG = 16;

    public final static int SER_NUMBER_FLOAT = 17;

    public final static int SER_NUMBER_DOUBLE = 18;

    public final static int SER_NUMBER_BIG_INTEGER = 19;

    public final static int SER_NUMBER_BIG_DECIMAL = 20;

    // // // Other specific scalar types

    public final static int SER_BOOLEAN = 21;
    public final static int SER_CHAR = 22;

    public final static int SER_ENUM = 23;

    public final static int SER_DATE = 24;
    public final static int SER_CALENDAR = 25;

    public final static int SER_CLASS = 26;
    public final static int SER_FILE = 27;
    public final static int SER_UUID = 28;
    public final static int SER_URL = 29;
    public final static int SER_URI = 30;


    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    public final static int SER_ITERABLE = 31;

    /*
    /**********************************************************************
    /* Caching
    /**********************************************************************
     */

    /**
     * Mapping from classes to resolved type constants or indexes, to use
     * for serialization.
     */
    protected final ConcurrentHashMap<ClassKey, Integer> _knownSerTypes;
    
    /**
     * Set of Bean types that have been resolved.
     */
    protected final CopyOnWriteArrayList<BeanDefinition> _knownBeans;
    
    /*
    /**********************************************************************
    /* Instance state
    /**********************************************************************
     */
    
    protected ClassKey _key = new ClassKey();

    /**
     * Whether this instance is used for serialization (true)
     * or deserialization (false).
     */
    protected final boolean _forSerialization;
    
    protected Class<?> _prevClass;

    protected int _prevType;

    protected int _features;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */
    
    protected TypeDetector(boolean forSerialization,
            ConcurrentHashMap<ClassKey, Integer> types,
            CopyOnWriteArrayList<BeanDefinition> beans,
            int features) {
        _forSerialization = forSerialization;
        _knownSerTypes = types;
        _knownBeans = beans;
        _features = features;
    }

    protected TypeDetector(TypeDetector base, int features) {
        _forSerialization = base._forSerialization;
        _knownSerTypes = base._knownSerTypes;
        _knownBeans = base._knownBeans;
        _features = features;
    }

    public final static TypeDetector rootDetector(boolean forSerialization, int features) {
        return new TypeDetector(forSerialization,
                new ConcurrentHashMap<ClassKey, Integer>(50, 0.75f, 4),
                new CopyOnWriteArrayList<BeanDefinition>(),
                features);
    }

    public TypeDetector perOperationInstance(int features) {
        return new TypeDetector(this, features);
    }

    public BeanDefinition getBeanDefinition(int index) {
        // for simplicity, let's allow caller to pass negative id as is
        if (index < 0) {
            index = -(index+1);
        }
        return _knownBeans.get(index);
    }
    
    /**
     * The main lookup method used to find type identifier for
     * given raw class; including Bean types (if allowed).
     */
    public final int findFullType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        int type;

        Integer I = _knownSerTypes.get(k);

        if (I == null) {
            type = _findFull(raw);
            _knownSerTypes.put(new ClassKey(raw), Integer.valueOf(type));
        } else {
            type = I.intValue();
        }
        _prevType = type;
        _prevClass = raw;
        return type;
    }

    /*
    // Lookup method used to find type identifier for
    // given raw class, if (and only if) it is a "simple" type, not a Bean type.
    public final int findSimpleSerializationType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        int type;

        Integer I = _knownTypes.get(k);

        if (I == null) {
            type = _findSimple(raw);
            if (type == SER_UNKNOWN) {
                return type;
            }
            _knownTypes.put(k, Integer.valueOf(type));
        } else {
            type = I.intValue();
        }
        _prevType = type;
        _prevClass = raw;
        return type;
    }
    */

    protected int _findFull(Class<?> raw)
    {
        int type = _findSimple(raw);
        if (type == SER_UNKNOWN) {
            if (JSON.Feature.HANDLE_JAVA_BEANS.isEnabled(_features)) {
                BeanDefinition def = resolveBean(raw);
                // Due to concurrent access, possible that someone might have added it
                synchronized (_knownBeans) {
                    // Important: do NOT try to reuse shared instance; caller needs it
                    ClassKey k = new ClassKey(raw);
                    Integer I = _knownSerTypes.get(k);
                    // if it was already concurrently added, we'll just discard this copy, return earlier
                    if (I != null) {
                        return I.intValue();
                    }
                    // otherwise add at the end, use -(index+1) as id
                    _knownBeans.add(def);
                    int typeId = -_knownBeans.size();
    
                    _knownSerTypes.put(k, Integer.valueOf(typeId));
                    return typeId;
                }
            }
        }
        return type;
    }

    protected int _findSimple(Class<?> raw)
    {
        if (raw == String.class) {
            return SER_STRING;
        }
        if (raw.isArray()) {
            Class<?> elemType = raw.getComponentType();
            if (elemType.isPrimitive()) {
                if (raw == byte[].class) {
                    return SER_BYTE_ARRAY;
                }
                if (raw == char[].class) {
                    return SER_CHAR_ARRAY;
                }
                if (raw == int[].class) {
                    return SER_INT_ARRAY;
                }
                if (raw == long[].class) {
                    return SER_LONG_ARRAY;
                }
                if (raw == boolean[].class) {
                    return SER_BOOLEAN_ARRAY;
                }
                // Hmmh. Could support all types; add as/when needed
                return SER_UNKNOWN;
            }
            if (elemType == Object.class || _forSerialization) {
                return SER_OBJECT_ARRAY;
            }
            return SER_UNKNOWN;
        }
        if (raw.isPrimitive()) {
            if (raw == Boolean.TYPE) return SER_BOOLEAN;
            if (raw == Integer.TYPE) return SER_NUMBER_INTEGER;
            if (raw == Long.TYPE) return SER_NUMBER_LONG;
            if (raw == Byte.TYPE) return SER_NUMBER_BYTE;
            if (raw == Short.TYPE) return SER_NUMBER_SHORT;
            if (raw == Double.TYPE) return SER_NUMBER_DOUBLE;
            if (raw == Float.TYPE) return SER_NUMBER_FLOAT;
            if (raw == Character.TYPE) return SER_CHAR;
            throw new IllegalArgumentException("Unrecognized primitive type: "+raw.getName());
        }
        if (raw == Boolean.class) {
            return SER_BOOLEAN;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) return SER_NUMBER_INTEGER;
            if (raw == Long.class) return SER_NUMBER_LONG;
            if (raw == Byte.class) return SER_NUMBER_BYTE;
            if (raw == Short.class) return SER_NUMBER_SHORT;
            if (raw == Double.class) return SER_NUMBER_DOUBLE;
            if (raw == Float.class) return SER_NUMBER_FLOAT;
            if (raw == BigDecimal.class) return SER_NUMBER_BIG_DECIMAL;
            if (raw == BigInteger.class) {
                return SER_NUMBER_BIG_INTEGER;
            }
            // What numeric type is this? Could consider "string-like" but...
            return SER_UNKNOWN;
        }
        if (raw == Character.class) {
            return SER_CHAR;
        }
        if (raw.isEnum()) {
            return SER_ENUM;
        }
        if (Map.class.isAssignableFrom(raw)) {
            return SER_MAP;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (List.class.isAssignableFrom(raw)) {
                // One more thing: here we assume LIST means efficient random access
                if (RandomAccess.class.isAssignableFrom(raw)) {
                    return SER_LIST;
                }
                // and if not, consider "only" a collection
            }
            return SER_COLLECTION;
        }
        if (TreeNode.class.isAssignableFrom(raw)) {
            // should we require more accurate type for deser?
            return SER_TREE_NODE;
        }
        // Misc String-like types
        if (Calendar.class.isAssignableFrom(raw)) {
            return SER_CALENDAR;
        }
        if (raw == Class.class) {
            return SER_CLASS;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return SER_DATE;
        }
        if (File.class.isAssignableFrom(raw)) {
            return SER_FILE;
        }
        if (URL.class.isAssignableFrom(raw)) {
            return SER_URL;
        }
        if (URI.class.isAssignableFrom(raw)) {
            return SER_URI;
        }
        if (UUID.class.isAssignableFrom(raw)) {
            return SER_UUID;
        }
        /* May or may not help with deser, but recognized nonetheless;
         * on assumption that Beans should rarely implement `CharSequence`
         */
        if (CharSequence.class.isAssignableFrom(raw)) {
            return SER_CHARACTER_SEQUENCE;
        }
        /* `Iterable` can be added on all kinds of things, and it won't
         * help at all with deserialization; hence only use for serialization.
         */
        if (_forSerialization && Iterable.class.isAssignableFrom(raw)) {
            return SER_ITERABLE;
        }
        
        // Ok. I give up, no idea!
        return SER_UNKNOWN;
    }

    protected final BeanProperty[] NO_PROPS = new BeanProperty[0];

    // public just to make test accessible
    public BeanDefinition resolveBean(Class<?> raw)
    {
        final boolean forceAccess = JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features);
        // note: ignore methods in `java.lang.Object` (like "getClass()")
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(raw, Object.class);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Failed to introspect BeanInfo for type '"
                    +raw.getName()+"': "+e.getMessage(), e);
        }
        List<BeanProperty> props = new ArrayList<BeanProperty>();
        for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
            // no type means indexed property
            Class<?> type = prop.getPropertyType();
            if (type == null) {
                continue;
            }
            final Method readMethod = prop.getReadMethod();
            final Method writeMethod = prop.getWriteMethod();
            String name;
            if (readMethod != null) {
                name = readMethod.getName();
                if (name.startsWith("get")) {
                    name = Introspector.decapitalize(name.substring(3));
                } else if (name.startsWith("is")) {
                    name = Introspector.decapitalize(name.substring(3));
                } else {
                    name = Introspector.decapitalize(name);
                }
            } else if (writeMethod != null) {
                name = writeMethod.getName();
                if (name.startsWith("set")) {
                    name = Introspector.decapitalize(name.substring(3));
                } else {
                    name = Introspector.decapitalize(name);
                }
            } else { // can this happen?
                continue;
            }
            // ok, two things: maybe we can pre-resolve the type?
            int typeId = _findSimple(type);
            if (forceAccess) {
                if (readMethod != null) {
                    readMethod.setAccessible(true);
                }
                if (writeMethod != null) {
                    writeMethod.setAccessible(true);
                }
            }
            BeanProperty bp = new BeanProperty(name, type, typeId, readMethod, writeMethod);
            props.add(bp);
        }
        if (_forSerialization) {
            final int len = props.size();
            BeanProperty[] propArray = (len == 0) ? NO_PROPS
                    : props.toArray(new BeanProperty[len]);
            return new BeanDefinition(raw, propArray);
        }

        Constructor<?> defaultCtor = null;
        Constructor<?> stringCtor = null;
        Constructor<?> longCtor = null;

        for (Constructor<?> ctor : raw.getDeclaredConstructors()) {
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
            if (forceAccess) {
                ctor.setAccessible(true);
            }
        }

        Map<String, BeanProperty> propMap = new HashMap<String, BeanProperty>();
        for (BeanProperty prop : props) {
            propMap.put(prop.getName().toString(), prop);
        }
        return new BeanDefinition(raw, propMap,
                defaultCtor, stringCtor, longCtor);
    }
}
