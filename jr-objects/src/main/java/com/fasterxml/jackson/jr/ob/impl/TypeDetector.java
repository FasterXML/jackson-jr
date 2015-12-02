package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.type.ResolvedType;
import com.fasterxml.jackson.jr.type.TypeBindings;
import com.fasterxml.jackson.jr.type.TypeResolver;

/**
 * Helper object used for efficient detection of type information
 * relevant to our conversion needs when writing out Java Objects
 * as JSON.
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
    protected final BeanProperty[] NO_PROPS = new BeanProperty[0];

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
    /* Helper objects, serialization
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
    /* Helper objects, deserialization
    /**********************************************************************
     */

    /**
     * For generic containers (Collections, Maps, arrays), we may need
     * this guy.
     */
    protected final TypeResolver _typeResolver;
    
    /**
     * Set of {@link ValueReader}s that we have resolved
     */
    protected final ConcurrentHashMap<ClassKey, ValueReader> _knownReaders;

    /**
     * During resolution, some readers may be in-progress, but need to be
     * linked: for example, with cyclic type references.
     */
    protected Map<ClassKey, ValueReader> _incompleteReaders;

    protected final Object _readerLock;
    
    /*
    /**********************************************************************
    /* Instance state
    /**********************************************************************
     */
    
    protected ClassKey _key = new ClassKey();
    
    protected Class<?> _prevClass;

    protected int _prevType;

    protected int _features;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    // for serialization
    protected TypeDetector(int features,
            ConcurrentHashMap<ClassKey, Integer> types,
            CopyOnWriteArrayList<BeanDefinition> beans) {
        _features = features;
        _knownSerTypes = types;
        _knownBeans = beans;
        _knownReaders = null;
        _typeResolver = null;
        _readerLock = null;
    }

    // for deserialization
    protected TypeDetector(int features,
            ConcurrentHashMap<ClassKey, ValueReader> r) {
        _features = features;
        _knownSerTypes = null;
        _knownBeans = null;
        _knownReaders = r;
        _typeResolver = new TypeResolver();
        _readerLock = new Object();
    }
    
    protected TypeDetector(TypeDetector base, int features) {
        _features = features;
        _knownSerTypes = base._knownSerTypes;
        _knownBeans = base._knownBeans;
        _knownReaders = base._knownReaders;
        _typeResolver = base._typeResolver;
        _readerLock = base._readerLock;
    }

    public final static TypeDetector forReader(int features) {
        return new TypeDetector(features,
                new ConcurrentHashMap<ClassKey, ValueReader>(50, 0.75f, 4));
    }

    public final static TypeDetector forWriter(int features) {
        return new TypeDetector(features,
                new ConcurrentHashMap<ClassKey, Integer>(50, 0.75f, 4),
                new CopyOnWriteArrayList<BeanDefinition>());
    }
    
    public TypeDetector perOperationInstance(int features) {
        return new TypeDetector(this, features);
    }

    private boolean forSer() {
        return _knownSerTypes != null;
    }
    
    /*
    /**********************************************************************
    /* Methods for ser and deser
    /**********************************************************************
     */
    
    protected BeanDefinition resolveBean(Class<?> raw)
    {
        final boolean forceAccess = JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features);
        final boolean forSer = forSer();
        
        List<BeanProperty> props0;

        try {
            props0 = _introspectBean(raw, forSer, forceAccess);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to introspect BeanInfo for type '"
                    +raw.getName()+"': "+e.getMessage(), e);
        }
        if (forSer) {
            ArrayList<BeanProperty> props = new ArrayList<BeanProperty>(props0.size());
            // Need to pre-resolve the type?
            for (BeanProperty prop : props0) {
                prop = prop.withTypeId(_findSimple(prop.rawGetterType()));
                props.add(prop);
            }
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
        for (BeanProperty prop : props0) {
            propMap.put(prop.getName().toString(), prop);
        }
        return new BeanDefinition(raw, propMap,
                defaultCtor, stringCtor, longCtor);
    }

    protected List<BeanProperty> _introspectBean(Class<?> beanType, boolean forSer, boolean forceAccess)
    {
        Map<String,BeanProperty> props = new TreeMap<String,BeanProperty>();
        _introspect(beanType, props);
        List<BeanProperty> result = new ArrayList<BeanProperty>(props.size());

        for (BeanProperty prop : props.values()) {
            // First: weed out props without proper accessor
            if (forSer) {
                if (!prop.hasGetter()) {
                    continue;
                }
            } else {
                if (!prop.hasSetter()) {
                    continue;
                }
            }
            
            // and if it's fit, force access as needed
            if (forceAccess) {
                prop.forceAccess();
            }
            result.add(prop);
        }
        return result;
    }

    protected void _introspect(Class<?> currType, Map<String,BeanProperty> props)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props);
        // then get methods from within this class
        
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            if (Modifier.isStatic(flags)) {
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
                        BeanProperty prop = _propFrom(props, name);
                        props.put(name, prop.withGetter(m));
                    }
                } else if (name.startsWith("is") && name.length() > 2) {
                    // 28-Jul-2014, tatu: Stupid misnaming complicates things here;
                    //   basically, until we remove wrong one, need to require both
                    //   to be set...
                    if (JSON.Feature.USE_IS_GETTERS.isEnabled(_features)) {
                        // only add if no getter found (i.e. prefer regular getter, if one found)
                        BeanProperty prop = props.get(name);
                        if (prop == null) {
                            name = decap(name.substring(2));
                            props.put(name, new BeanProperty(name).withGetter(m));
                        } else if (!prop.hasGetter()) {
                            name = decap(name.substring(2));
                            props.put(name, prop.withGetter(m));
                        }
                    }
                }
            } else if (argTypes.length == 1) { // setter?
                // Non-public setters are fine, if we can force access
                if (!Modifier.isPublic(flags) && !JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features)) {
                    continue;
                }
                // but let's not bother about return type; setters that return value are fine
                String name = m.getName();
                if (!name.startsWith("set") || name.length() == 3) {
                    continue;
                }
                name = decap(name.substring(3));
                BeanProperty prop = _propFrom(props, name);
                props.put(name, prop.withSetter(m));
            }
        }
    }

    private BeanProperty _propFrom(Map<String,BeanProperty> props, String name) {
        BeanProperty prop = props.get(name);
        if (prop == null) {
            prop = new BeanProperty(name);
        }
        return prop;
    }
    
    /*
    /**********************************************************************
    /* Methods for serialization
    /**********************************************************************
     */
    
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
        ClassKey k = _key.with(raw);
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
            return SER_OBJECT_ARRAY;
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
        if (forSer() && Iterable.class.isAssignableFrom(raw)) {
            return SER_ITERABLE;
        }
        
        // Ok. I give up, no idea!
        return SER_UNKNOWN;
    }

    /*
    /**********************************************************************
    /* Methods for deserialization
    /**********************************************************************
     */

    /**
     * Method used during deserialization to find handler for given
     * non-generic type.
     */
    public ValueReader findReader(Class<?> raw)
    {
        ClassKey k = _key.with(raw);

        ValueReader vr = _knownReaders.get(k);
        if (vr != null) {
            return vr;
        }
        vr = createReader(null, raw, raw);
        _knownReaders.putIfAbsent(new ClassKey(raw), vr);
        return vr;
    }
    
    private ValueReader createReader(Class<?> contextType, Class<?> type,
            Type genericType)
    {
        if (type == Object.class) {
            return AnyReader.std;
        }
        if (type.isArray()) {
            Class<?> elemType = type.getComponentType();
            if (!elemType.isPrimitive()) {
                return new ArrayReader(elemType, createReader(contextType, elemType, elemType));
            }
            int typeId = _findSimple(type);
            if (typeId > 0) {
                return new SimpleValueReader(typeId, type);
            }
            throw new IllegalArgumentException("Deserialization of "+type.getName()+" not (yet) supported");
        }
        if (type.isEnum()) {
            return enumReader(type);
        }
        if (Collection.class.isAssignableFrom(type)) {
            return collectionReader(contextType, genericType);
        }
        if (Map.class.isAssignableFrom(type)) {
            return mapReader(contextType, genericType);
        }
        int typeId = _findSimple(type);
        if (typeId > 0) {
            return new SimpleValueReader(typeId, type);
        }
        // Beans!
        final ClassKey key = new ClassKey(type);
        synchronized (_readerLock) {
            if (_incompleteReaders == null) {
                _incompleteReaders = new HashMap<ClassKey, ValueReader>();
            } else { // perhaps it has already been resolved?
                ValueReader vr = _incompleteReaders.get(new ClassKey(type));
                if (vr != null) {
                    return vr;
                }
            }
            BeanDefinition def = resolveBean(type);
            try {
                _incompleteReaders.put(key, def);
                for (Map.Entry<String, BeanProperty> entry : def.propertiesByName().entrySet()) {
                    BeanProperty prop = entry.getValue();
                    entry.setValue(prop.withReader(createReader(contextType,
                            prop.rawSetterType(), prop.genericSetterType())));
                }
            } finally {
                _incompleteReaders.remove(key);
            }
            return def;
        }
    }

    private TypeBindings bindings(Class<?> ctxt) {
        if (ctxt == null) {
            return TypeBindings.emptyBindings();
        }
        return TypeBindings.create(ctxt, (ResolvedType[]) null);
    }
    
    public static ValueReader enumReader(Class<?> enumType) {
        Object[] enums = enumType.getEnumConstants();
        Map<String,Object> byName = new HashMap<String,Object>();
        for (Object e : enums) {
            byName.put(e.toString(), e);
        }
        return new EnumReader(enums, byName);
    }

    protected ValueReader collectionReader(Class<?> contextType, Type collectionType)
    {
        ResolvedType t = _typeResolver.resolve(bindings(contextType), collectionType);
        List<ResolvedType> params = t.typeParametersFor(Collection.class);
        return collectionReader(t.erasedType(), params.get(0));
    }

    protected ValueReader collectionReader(Class<?> collectionType, ResolvedType valueType)
    {
        Class<?> raw = valueType.erasedType();
        if (Collection.class.isAssignableFrom(raw)) {
            List<ResolvedType> params = valueType.typeParametersFor(Collection.class);
            return collectionReader(raw, params.get(0));
        }
        if (Map.class.isAssignableFrom(raw)) {
            List<ResolvedType> params = valueType.typeParametersFor(Map.class);
            return mapReader(raw, params.get(1));
        }
        return new CollectionReader(collectionType, createReader(null, raw, raw));
    }

    protected ValueReader mapReader(Class<?> contextType, Type mapType)
    {
        ResolvedType t = _typeResolver.resolve(bindings(contextType), mapType);
        List<ResolvedType> params = t.typeParametersFor(Map.class);
        return mapReader(t.erasedType(), params.get(1));
    }
    
    protected ValueReader mapReader(Class<?> mapType, ResolvedType valueType)
    {
        Class<?> raw = valueType.erasedType();
        if (Collection.class.isAssignableFrom(raw)) {
            List<ResolvedType> params = valueType.typeParametersFor(Collection.class);
            return collectionReader(raw, params.get(0));
        }
        if (Map.class.isAssignableFrom(raw)) {
            List<ResolvedType> params = valueType.typeParametersFor(Map.class);
            return mapReader(raw, params.get(1));
        }
        return new MapReader(mapType, createReader(null, raw, raw));
    }

    /*
    /**********************************************************************
    /* Other helper methods
    /**********************************************************************
     */
    
    private static String decap(String name) {
        char c = name.charAt(0);
        if (name.length() > 1
                && Character.isUpperCase(name.charAt(1))
                && Character.isUpperCase(c)){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(c);
        return new String(chars);
    }
}
