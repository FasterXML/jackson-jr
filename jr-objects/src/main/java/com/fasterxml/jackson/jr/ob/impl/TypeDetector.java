package com.fasterxml.jackson.jr.ob.impl;

import java.beans.*;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.TreeNode;
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

    /**
     * An implementation of {@link com.fasterxml.jackson.core.TreeNode}
     */
    public final static int SER_TREE_NODE = 5;
    
    // // // String(-like) types

    public final static int SER_STRING = 6;
    
    /**
     * General "misc thing that can be serialized by calling 'toString()" type,
     * used as a fallback for things that are barely recognized.
     */
    public final static int SER_STRING_LIKE = 7;

    public final static int SER_CHARACTER_SEQUENCE = 8;
    
    public final static int SER_CHAR_ARRAY = 9;

    public final static int SER_BYTE_ARRAY = 10;

    public final static int SER_INT_ARRAY = 11;

    // // // Numbers
    
    public final static int SER_NUMBER_BYTE = 12;

    public final static int SER_NUMBER_SHORT = 13;
    
    public final static int SER_NUMBER_INTEGER = 14;

    public final static int SER_NUMBER_LONG = 15;

    public final static int SER_NUMBER_FLOAT = 16;

    public final static int SER_NUMBER_DOUBLE = 17;

    public final static int SER_NUMBER_BIG_INTEGER = 18;

    public final static int SER_NUMBER_BIG_DECIMAL = 19;

    public final static int SER_NUMBER_OTHER = 20;
    
    // // // Other specific scalar types
    
    public final static int SER_BOOLEAN = 21;

    public final static int SER_CHAR = 22;

    public final static int SER_DATE = 23;

    public final static int SER_ENUM = 24;

    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    public final static int SER_ITERABLE = 25;

    /*
    /**********************************************************************
    /* Value constants for de-serialization; need to be separate since
    /* aspects (ser vs deser) are often asymmetric
    /**********************************************************************
     */
    
    /*
    /**********************************************************************
    /* Caching
    /**********************************************************************
     */

    /**
     * Mapping from classes to resolved type constants or indexes.
     */
    protected final ConcurrentHashMap<ClassKey, Integer> _knownTypes;

    /**
     * Set of Bean types that have been resolved.
     */
    protected final CopyOnWriteArrayList<BeanDefinition> _knownBeans;
    
    /*
    /**********************************************************************
    /* Instance state
    /**********************************************************************
     */
    
    protected final ClassKey _key = new ClassKey();
    
    protected Class<?> _prevClass;

    protected int _prevType;

    protected int _features;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */
    
    protected TypeDetector(ConcurrentHashMap<ClassKey, Integer> types,
            CopyOnWriteArrayList<BeanDefinition> beans,
            int features) {
        _knownTypes = types;
        _knownBeans = beans;
        _features = features;
    }

    protected TypeDetector(TypeDetector base, int features) {
        _knownTypes = base._knownTypes;
        _knownBeans = base._knownBeans;
        _features = features;
    }
    
    public final static TypeDetector rootDetector(int features) {
        return new TypeDetector(new ConcurrentHashMap<ClassKey, Integer>(50, 0.75f, 4),
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
    public final int findFullSerializationType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        int type;

        Integer I = _knownTypes.get(k);

        if (I == null) {
            type = _findFull(raw);
            _knownTypes.put(k, Integer.valueOf(type));
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
                BeanDefinition def = _resolveBean(raw);
                // Due to concurrent access, possible that someone might have added it
                synchronized (_knownBeans) {
                    // Important: do NOT try to reuse shared instance; caller needs it
                    ClassKey k = new ClassKey(raw);
                    Integer I = _knownTypes.get(k);
                    // if it was already concurrently added, we'll just discard this copy, return earlier
                    if (I != null) {
                        return I.intValue();
                    }
                    // otherwise add at the end, use -(index+1) as id
                    _knownBeans.add(def);
                    int typeId = -_knownBeans.size();
    
                    _knownTypes.put(k, Integer.valueOf(typeId));
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
                // Hmmh. Could support all types but....
                return SER_UNKNOWN;
            }
            return SER_OBJECT_ARRAY;
        }
        if (raw == Boolean.class) {
            return SER_BOOLEAN;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) {
                return SER_NUMBER_INTEGER;
            }
            if (raw == Long.class) {
                return SER_NUMBER_LONG;
            }
            if (raw == Byte.class) {
                return SER_NUMBER_BYTE;
            }
            if (raw == Short.class) {
                return SER_NUMBER_SHORT;
            }
            if (raw == Float.class) {
                return SER_NUMBER_FLOAT;
            }
            if (raw == Double.class) {
                return SER_NUMBER_DOUBLE;
            }
            if (raw == BigDecimal.class) {
                return SER_NUMBER_BIG_DECIMAL;
            }
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
            return SER_TREE_NODE;
        }
        if (CharSequence.class.isAssignableFrom(raw)) {
            return SER_CHARACTER_SEQUENCE;
        }
        // Misc String-like types
        if (UUID.class.isAssignableFrom(raw)
                || File.class.isAssignableFrom(raw)
                || URL.class.isAssignableFrom(raw)
                || URI.class.isAssignableFrom(raw)
                ) {
            return SER_STRING_LIKE;
        }
        if (Iterable.class.isAssignableFrom(raw)) {
            return SER_ITERABLE;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return SER_DATE;
        }
        // Ok. I give up, no idea!
        return SER_UNKNOWN;
    }

    protected final BeanProperty[] NO_PROPS = new BeanProperty[0];

    protected BeanDefinition _resolveBean(Class<?> raw)
    {
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
            // One more thing: force access if need be:
            if (JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features)) {
                if (readMethod != null) {
                    readMethod.setAccessible(true);
                }
                // no use for write method yet; if we had, should force access
            }
            // ok, two things: maybe we can pre-resolve the type?
            BeanProperty bp = new BeanProperty(name, type, readMethod, writeMethod);
            int typeId = _findSimple(type);
            if (typeId != SER_UNKNOWN) {
                bp = bp.withWriteTypeId(typeId);
            }
            props.add(bp);
        }
        final int len = props.size();
        BeanProperty[] propArray = (len == 0) ? NO_PROPS
                : props.toArray(new BeanProperty[len]);

        return new BeanDefinition(raw, propArray);
    }
}
