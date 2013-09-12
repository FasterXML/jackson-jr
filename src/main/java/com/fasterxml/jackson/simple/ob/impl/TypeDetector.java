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
    
    protected final ConcurrentHashMap<ClassKey, ValueType> _knownTypes;

    protected final ClassKey _key = new ClassKey();
    
    protected Class<?> _prevClass;

    protected ValueType _prevType;

    protected TypeDetector(ConcurrentHashMap<ClassKey, ValueType> types) {
        _knownTypes = types;
    }

    protected TypeDetector(TypeDetector base) {
        _knownTypes = base._knownTypes;
    }
    
    public final static TypeDetector rootDetector() {
        return new TypeDetector(new ConcurrentHashMap<ClassKey, ValueType>(50, 0.75f, 4));
    }

    public TypeDetector perOperationInstance() {
        // Let's try to keep mem usage bounded; safe, if not super clean:
        if (_knownTypes.size() >= MAX_ENTRIES) {
            _knownTypes.clear();
        }
        return new TypeDetector(this);
    }

    public final ValueType findType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        ValueType t = _knownTypes.get(k);
        if (t == null) {
            t = _find(raw);
        }
        _prevType = t;
        _prevClass = raw;
        return t;
    }

    protected ValueType _find(Class<?> raw)
    {
        if (raw == String.class) {
            return ValueType.STRING;
        }
        if (raw.isArray()) {
            Class<?> elemType = raw.getComponentType();
            if (elemType.isPrimitive()) {
                if (raw == byte[].class) {
                    return ValueType.BYTE_ARRAY;
                }
                if (raw == char[].class) {
                    return ValueType.CHAR_ARRAY;
                }
                if (raw == int[].class) {
                    return ValueType.INT_ARRAY;
                }
                // Hmmh. Could support all types but....
                return ValueType.OTHER;
            }
            return ValueType.OBJECT_ARRAY;
        }
        if (raw == Boolean.class) {
            return ValueType.BOOLEAN;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) {
                return ValueType.NUMBER_INTEGER;
            }
            if (raw == Long.class) {
                return ValueType.NUMBER_LONG;
            }
            if (raw == Byte.class) {
                return ValueType.NUMBER_BYTE;
            }
            if (raw == Short.class) {
                return ValueType.NUMBER_SHORT;
            }
            if (raw == Float.class) {
                return ValueType.NUMBER_FLOAT;
            }
            if (raw == Double.class) {
                return ValueType.NUMBER_DOUBLE;
            }
            if (raw == BigDecimal.class) {
                return ValueType.NUMBER_BIG_DECIMAL;
            }
            if (raw == BigInteger.class) {
                return ValueType.NUMBER_BIG_INTEGER;
            }
            
            // What numeric type is this?!
            return ValueType.OTHER;
        }
        if (raw == Character.class) {
            return ValueType.CHAR;
        }
        if (raw.isEnum()) {
            return ValueType.ENUM;
        }
        if (Map.class.isAssignableFrom(raw)) {
            return ValueType.MAP;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (List.class.isAssignableFrom(raw)) {
                // One more thing: here we assume LIST means efficient random access
                if (RandomAccess.class.isAssignableFrom(raw)) {
                    return ValueType.LIST;
                }
                // and if not, consider "only" a collection
            }
            return ValueType.COLLECTION;
        }
        if (TreeNode.class.isAssignableFrom(raw)) {
            return ValueType.TREE_NODE;
        }
        if (CharSequence.class.isAssignableFrom(raw)) {
            return ValueType.CHARACTER_SEQUENCE;
        }
        if (Iterable.class.isAssignableFrom(raw)) {
            return ValueType.ITERABLE;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return ValueType.DATE;
        }
        
        // Ok. I give up, no idea!
        return ValueType.OTHER;
    }
}
