package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.lang.reflect.*;
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
    protected final BeanPropertyWriter[] NO_PROPS_FOR_WRITE = new BeanPropertyWriter[0];

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
    /* Other constants
    /**********************************************************************
     */

    protected final static int CACHE_FLAGS = JSON.Feature.cacheBreakers();

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

    protected final CopyOnWriteArrayList<BeanPropertyWriter[]> _knownWriters;

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

    /**
     * Reusable lookup key; only used by per-thread instances.
     */
    protected ClassKey _key;
    
    protected Class<?> _prevClass;

    protected int _prevType;

    protected int _features;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    /**
     * Constructor for the blueprint instance
     */
    protected TypeDetector(int features)
    {
        _features = features;
        _knownSerTypes = new ConcurrentHashMap<ClassKey, Integer>(50, 0.75f, 4);
        _knownWriters = new CopyOnWriteArrayList<BeanPropertyWriter[]>();
        _knownReaders = new ConcurrentHashMap<ClassKey, ValueReader>(50, 0.75f, 4);
        _typeResolver = new TypeResolver();
        _readerLock = new Object();
    }

    protected TypeDetector(TypeDetector base, int features) {
        _features = features;
        _knownSerTypes = base._knownSerTypes;
        _knownWriters = base._knownWriters;
        _knownReaders = base._knownReaders;
        _typeResolver = base._typeResolver;
        _readerLock = base._readerLock;
    }

    public final static TypeDetector blueprint(int features) {
        return new TypeDetector(features & CACHE_FLAGS);
    }

    public TypeDetector perOperationInstance(int features) {
        return new TypeDetector(this, features & CACHE_FLAGS);
    }

    /*
    /**********************************************************************
    /* Methods for ser and deser
    /**********************************************************************
     */

    protected POJODefinition resolvePOJODefinition(Class<?> raw)
    {
        try {
            return POJODefinition.find(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format
                    ("Failed to introspect ClassDefinition for type '%s': %s",
                    raw.getName(), e.getMessage()), e);
        }
    }

    protected int _findSimple(Class<?> raw, boolean forSer)
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
        if (forSer && Iterable.class.isAssignableFrom(raw)) {
            return SER_ITERABLE;
        }
        
        // Ok. I give up, no idea!
        return SER_UNKNOWN;
    }

    /*
    /**********************************************************************
    /* Methods for serialization
    /**********************************************************************
     */
    
    public BeanPropertyWriter[] getPropertyWriters(int index) {
        // for simplicity, let's allow caller to pass negative id as is
        if (index < 0) {
            index = -(index+1);
        }
        return _knownWriters.get(index);
    }
    
    /**
     * The main lookup method used to find type identifier for
     * given raw class; including Bean types (if allowed).
     */
    public final int findSerializationType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = (_key == null) ? new ClassKey(raw, _features) : _key.with(raw, _features);
        int type;

        Integer I = _knownSerTypes.get(k);

        if (I == null) {
            type = _findPOJOSerializationType(raw);
            _knownSerTypes.put(new ClassKey(raw, _features), Integer.valueOf(type));
        } else {
            type = I.intValue();
        }
        _prevType = type;
        _prevClass = raw;
        return type;
    }

    protected int _findPOJOSerializationType(Class<?> raw)
    {
        int type = _findSimple(raw, true);
        if (type == SER_UNKNOWN) {
            if (JSON.Feature.HANDLE_JAVA_BEANS.isEnabled(_features)) {
                POJODefinition cd = resolvePOJODefinition(raw);
                BeanPropertyWriter[] props = resolveBeanForSer(raw, cd);
                // Due to concurrent access, possible that someone might have added it
                synchronized (_knownWriters) {
                    // Important: do NOT try to reuse shared instance; caller needs it
                    ClassKey k = new ClassKey(raw, _features);
                    Integer I = _knownSerTypes.get(k);
                    // if it was already concurrently added, we'll just discard this copy, return earlier
                    if (I != null) {
                        return I.intValue();
                    }
                    // otherwise add at the end, use -(index+1) as id
                    _knownWriters.add(props);
                    int typeId = -_knownWriters.size();
                    _knownSerTypes.put(k, Integer.valueOf(typeId));
                    return typeId;
                }
            }
        }
        return type;
    }

    protected BeanPropertyWriter[] resolveBeanForSer(Class<?> raw, POJODefinition classDef)
    {
        POJODefinition.Prop[] rawProps = classDef.properties();
        final int len = rawProps.length;
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>(len);
        final boolean includeReadOnly = JSON.Feature.WRITE_READONLY_BEAN_PROPERTIES.isEnabled(_features);
        final boolean forceAccess = JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features);
        final boolean useFields = JSON.Feature.USE_FIELDS.isEnabled(_features);

        for (int i = 0; i < len; ++i) {
            POJODefinition.Prop rawProp = rawProps[i];
            Method m = rawProp.getter;
            if (m == null) {
                if (JSON.Feature.USE_IS_GETTERS.isEnabled(_features)) {
                    m = rawProp.isGetter;
                }
            }
            Field f = useFields ? rawProp.field : null;

            // But if neither regular nor is-getter, move on
            if ((m == null) && (f == null)) {
                continue;
            }
            // also: if setter is required to match, skip if none
            if (!includeReadOnly && !rawProp.hasSetter()) {
                continue;
            }
            Class<?> type;
            if (m != null) {
                type = m.getReturnType();
                if (forceAccess) {
                    m.setAccessible(true);
                }
            } else {
                type = f.getType();
                if (forceAccess) {
                    f.setAccessible(true);
                }
            }
            int typeId = _findSimple(type, true);
            props.add(new BeanPropertyWriter(typeId, rawProp.name, rawProp.field, m));
        }
        int plen = props.size();
        BeanPropertyWriter[] propArray = (plen == 0) ? NO_PROPS_FOR_WRITE
                : props.toArray(new BeanPropertyWriter[plen]);
        return propArray;
    }

    /*
    /**********************************************************************
    /* Methods for deserialization; simple factory methods
    /**********************************************************************
     */

    public ValueReader enumReader(Class<?> enumType) {
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
    /* Methods for deserialization; other
    /**********************************************************************
     */

    /**
     * Method used during deserialization to find handler for given
     * non-generic type.
     */
    public ValueReader findReader(Class<?> raw)
    {
        ClassKey k = (_key == null) ? new ClassKey(raw, _features) : _key.with(raw, _features);
        ValueReader vr = _knownReaders.get(k);
        if (vr != null) {
            return vr;
        }
        vr = createReader(null, raw, raw);
        _knownReaders.putIfAbsent(new ClassKey(raw, _features), vr);
        return vr;
    }
    
    protected ValueReader createReader(Class<?> contextType, Class<?> type,
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
            int typeId = _findSimple(type, false);
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
        int typeId = _findSimple(type, false);
        if (typeId > 0) {
            return new SimpleValueReader(typeId, type);
        }
        // Beans!
        final ClassKey key = new ClassKey(type, _features);
        synchronized (_readerLock) {
            if (_incompleteReaders == null) {
                _incompleteReaders = new HashMap<ClassKey, ValueReader>();
            } else { // perhaps it has already been resolved?
                ValueReader vr = _incompleteReaders.get(key);
                if (vr != null) {
                    return vr;
                }
            }
            BeanReader def = _resolveBeanForDeser(type);
            try {
                _incompleteReaders.put(key, def);
                for (Map.Entry<String, BeanPropertyReader> entry : def.propertiesByName().entrySet()) {
                    BeanPropertyReader prop = entry.getValue();
                    entry.setValue(prop.withReader(createReader(contextType,
                            prop.rawSetterType(), prop.genericSetterType())));
                }
            } finally {
                _incompleteReaders.remove(key);
            }
            return def;
        }
    }

    protected BeanReader _resolveBeanForDeser(Class<?> raw)
    {
        final POJODefinition pojoDef = resolvePOJODefinition(raw);

        Constructor<?> defaultCtor = pojoDef.defaultCtor;
        Constructor<?> stringCtor = pojoDef.stringCtor;
        Constructor<?> longCtor = pojoDef.longCtor;

        final boolean forceAccess = JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features);
        if (forceAccess) {
            if (defaultCtor != null) {
                defaultCtor.setAccessible(true);
            }
            if (stringCtor != null) {
                stringCtor.setAccessible(true);
            }
            if (longCtor != null) {
                longCtor.setAccessible(true);
            }
        }

        final POJODefinition.Prop[] rawProps = pojoDef.properties();
        Map<String, BeanPropertyReader> propMap = new HashMap<String, BeanPropertyReader>();
        final int len = rawProps.length;
        if (len == 0) {
            propMap = Collections.emptyMap();
        } else {
            final boolean useFields = JSON.Feature.USE_FIELDS.isEnabled(_features);
            propMap = new HashMap<String, BeanPropertyReader>();
            for (int i = 0; i < len; ++i) {
                POJODefinition.Prop rawProp = rawProps[i];                
                Method m = rawProp.setter;
                Field f = useFields ? rawProp.field : null;

                if (m != null) {
                    if (forceAccess) {
                        m.setAccessible(true);
                    } else if (!Modifier.isPublic(m.getModifiers())) {
                        // access to non-public setters must be forced to be usable:
                        m = null;
                    }
                }
                // if no setter, field would do as well
                if (m == null) {
                    if (f == null) {
                        continue;
                    }
                    // fields should always be public, but let's just double-check
                    if (forceAccess) {
                        f.setAccessible(true);
                    } else if (!Modifier.isPublic(f.getModifiers())) {
                        continue;
                    }
                }
                propMap.put(rawProp.name, new BeanPropertyReader(rawProp.name, f, m));
            }
        }
        return new BeanReader(raw, propMap,
                defaultCtor, stringCtor, longCtor);
    }

    private TypeBindings bindings(Class<?> ctxt) {
        if (ctxt == null) {
            return TypeBindings.emptyBindings();
        }
        return TypeBindings.create(ctxt, (ResolvedType[]) null);
    }
}
