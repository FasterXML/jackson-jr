package com.fasterxml.jackson.simple.ob.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.TreeNode;

/**
 * Helper object used for efficient detection of type information
 * relevant to our conversion needs.
 *<p>
 * Note that usage pattern is such that a single "root" instance is kept
 * by each {@link com.fasterxml.jackson.simple.ob.JSON} instance; and
 * an actual per-operation instance must be constructed by calling
 * {@link #perOperationInstance()}: reason for this is that instances
 * use simple caching to handle the common case of repeating types
 * within JSON Arrays.
 */
public class TypeDetector
{
    // TODO: can make configurable if it matters...
    protected final int MAX_ENTRIES = 1000;
    
    protected final ConcurrentHashMap<ClassKey, SimpleType> _knownTypes;

    protected final ClassKey _key = new ClassKey();
    
    protected Class<?> _prevClass;

    protected SimpleType _prevType;
    
    protected TypeDetector(ConcurrentHashMap<ClassKey, SimpleType> types) {
        _knownTypes = types;
    }

    protected TypeDetector(TypeDetector base) {
        _knownTypes = base._knownTypes;
    }
    
    public final static TypeDetector rootDetector() {
        return new TypeDetector(new ConcurrentHashMap<ClassKey, SimpleType>(50, 0.75f, 4));
    }

    public TypeDetector perOperationInstance() {
        // Let's try to keep mem usage bounded; safe, if not super clean:
        if (_knownTypes.size() >= MAX_ENTRIES) {
            _knownTypes.clear();
        }
        return new TypeDetector(this);
    }

    public final SimpleType findType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        SimpleType t = _knownTypes.get(k);
        if (t == null) {
            t = _find(raw);
        }
        _prevType = t;
        _prevClass = raw;
        return t;
    }

    protected SimpleType _find(Class<?> raw)
    {
        if (raw == String.class) {
            return SimpleType.STRING;
        }
        if (raw.isArray()) {
            Class<?> elemType = raw.getComponentType();
            if (elemType.isPrimitive()) {
                if (raw == byte[].class) {
                    return SimpleType.BYTE_ARRAY;
                }
                if (raw == char[].class) {
                    return SimpleType.CHAR_ARRAY;
                }
                if (raw == int[].class) {
                    return SimpleType.INT_ARRAY;
                }
                // Hmmh. Could support all types but....
                return SimpleType.OTHER;
            }
            return SimpleType.OBJECT_ARRAY;
        }
        if (raw == Boolean.class) {
            return SimpleType.BOOLEAN;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) {
                return SimpleType.NUMBER_INTEGER;
            }
            if (raw == Long.class) {
                return SimpleType.NUMBER_LONG;
            }
            if (raw == Byte.class) {
                return SimpleType.NUMBER_BYTE;
            }
            if (raw == Short.class) {
                return SimpleType.NUMBER_SHORT;
            }
            if (raw == Float.class) {
                return SimpleType.NUMBER_FLOAT;
            }
            if (raw == Double.class) {
                return SimpleType.NUMBER_DOUBLE;
            }
            if (raw == BigDecimal.class) {
                return SimpleType.NUMBER_BIG_DECIMAL;
            }
            if (raw == BigInteger.class) {
                return SimpleType.NUMBER_BIG_INTEGER;
            }
            
            // What numeric type is this?!
            return SimpleType.OTHER;
        }
        if (raw == Character.class) {
            return SimpleType.CHAR;
        }
        if (raw.isEnum()) {
            return SimpleType.ENUM;
        }
        if (Map.class.isAssignableFrom(raw)) {
            return SimpleType.MAP;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (List.class.isAssignableFrom(raw)) {
                // One more thing: here we assume LIST means efficient random access
                if (RandomAccess.class.isAssignableFrom(raw)) {
                    return SimpleType.LIST;
                }
                // and if not, consider "only" a collection
            }
            return SimpleType.COLLECTION;
        }
        if (TreeNode.class.isAssignableFrom(raw)) {
            return SimpleType.TREE_NODE;
        }
        if (CharSequence.class.isAssignableFrom(raw)) {
            return SimpleType.CHARACTER_SEQUENCE;
        }
        if (Iterable.class.isAssignableFrom(raw)) {
            return SimpleType.ITERABLE;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return SimpleType.DATE;
        }
        
        // Ok. I give up, no idea!
        return SimpleType.OTHER;
    }
}
