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
    /* Value constants
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
     * Type not otherwise recognized.
     */
    public final static int VT_OTHER = 0;
    
    /**
     * All kinds of {@link java.util.Map}s.
     */
    public final static int VT_MAP = 1;

    /**
     * All kinds of {@link java.util.List}s.
     */
    public final static int VT_LIST = 2;

    /**
     * All kinds of {@link java.util.Collection}s other than {@link java.util.List}s
     */
    public final static int VT_COLLECTION = 3;

    /**
     * Arrays of non-primitive types
     */
    public final static int VT_OBJECT_ARRAY = 4;

    /**
     * An implementation of {@link com.fasterxml.jackson.core.TreeNode}
     */
    public final static int VT_TREE_NODE = 5;
    
    // // // String(-like) types

    public final static int VT_STRING = 6;
    
    /**
     * General "misc thing that can be serialized by calling 'toString()" type,
     * used as a fallback for things that are barely recognized.
     */
    public final static int VT_STRING_LIKE = 7;

    public final static int VT_CHARACTER_SEQUENCE = 8;
    
    public final static int VT_CHAR_ARRAY = 9;

    public final static int VT_BYTE_ARRAY = 10;

    public final static int VT_INT_ARRAY = 11;

    // // // Numbers
    
    public final static int VT_NUMBER_BYTE = 12;

    public final static int VT_NUMBER_SHORT = 13;
    
    public final static int VT_NUMBER_INTEGER = 14;

    public final static int VT_NUMBER_LONG = 15;

    public final static int VT_NUMBER_FLOAT = 16;

    public final static int VT_NUMBER_DOUBLE = 17;

    public final static int VT_NUMBER_BIG_INTEGER = 18;

    public final static int VT_NUMBER_BIG_DECIMAL = 19;

    public final static int VT_NUMBER_OTHER = 20;
    
    // // // Other specific scalar types
    
    public final static int VT_BOOLEAN = 21;

    public final static int VT_CHAR = 22;

    public final static int VT_DATE = 23;

    public final static int VT_ENUM = 24;

    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    public final static int VT_ITERABLE = 25;

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

    public BeanDefinition findBean(int index) {
        return _knownBeans.get(index);
    }
    
    /**
     * The main lookup method used to find type identifier for
     * given raw class.
     */
    public final int findType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        int type;

        Integer I = _knownTypes.get(k);

        if (I == null) {
            type = _find(raw);
            _knownTypes.put(k, Integer.valueOf(type));
        } else {
            type = I.intValue();
        }
        _prevType = type;
        _prevClass = raw;
        return type;
    }

    protected int _find(Class<?> raw)
    {
        if (raw == String.class) {
            return VT_STRING;
        }
        if (raw.isArray()) {
            Class<?> elemType = raw.getComponentType();
            if (elemType.isPrimitive()) {
                if (raw == byte[].class) {
                    return VT_BYTE_ARRAY;
                }
                if (raw == char[].class) {
                    return VT_CHAR_ARRAY;
                }
                if (raw == int[].class) {
                    return VT_INT_ARRAY;
                }
                // Hmmh. Could support all types but....
                return VT_OTHER;
            }
            return VT_OBJECT_ARRAY;
        }
        if (raw == Boolean.class) {
            return VT_BOOLEAN;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) {
                return VT_NUMBER_INTEGER;
            }
            if (raw == Long.class) {
                return VT_NUMBER_LONG;
            }
            if (raw == Byte.class) {
                return VT_NUMBER_BYTE;
            }
            if (raw == Short.class) {
                return VT_NUMBER_SHORT;
            }
            if (raw == Float.class) {
                return VT_NUMBER_FLOAT;
            }
            if (raw == Double.class) {
                return VT_NUMBER_DOUBLE;
            }
            if (raw == BigDecimal.class) {
                return VT_NUMBER_BIG_DECIMAL;
            }
            if (raw == BigInteger.class) {
                return VT_NUMBER_BIG_INTEGER;
            }
            
            // What numeric type is this?!
            return VT_OTHER;
        }
        if (raw == Character.class) {
            return VT_CHAR;
        }
        if (raw.isEnum()) {
            return VT_ENUM;
        }
        if (Map.class.isAssignableFrom(raw)) {
            return VT_MAP;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (List.class.isAssignableFrom(raw)) {
                // One more thing: here we assume LIST means efficient random access
                if (RandomAccess.class.isAssignableFrom(raw)) {
                    return VT_LIST;
                }
                // and if not, consider "only" a collection
            }
            return VT_COLLECTION;
        }
        if (TreeNode.class.isAssignableFrom(raw)) {
            return VT_TREE_NODE;
        }
        if (CharSequence.class.isAssignableFrom(raw)) {
            return VT_CHARACTER_SEQUENCE;
        }
        // Misc String-like types
        if (UUID.class.isAssignableFrom(raw)
                || File.class.isAssignableFrom(raw)
                || URL.class.isAssignableFrom(raw)
                || URI.class.isAssignableFrom(raw)
                ) {
            return VT_STRING_LIKE;
        }
        if (Iterable.class.isAssignableFrom(raw)) {
            return VT_ITERABLE;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return VT_DATE;
        }
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
        // Ok. I give up, no idea!
        return VT_OTHER;
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
            props.add(new BeanProperty(name, readMethod, writeMethod));
        }
        final int len = props.size();
        BeanProperty[] propArray = (len == 0) ? NO_PROPS
                : props.toArray(new BeanProperty[len]);

        return new BeanDefinition(raw, propArray);
    }
}
