package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;

import static com.fasterxml.jackson.jr.ob.impl.Types.isEnum;

// Only public for reference by `AnnotationBasedValueRWModifier`
public abstract class ValueLocatorBase
{
    /*
    /**********************************************************************
    /* Value constants for "simple" types
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
    public final static int SER_NUMBER_INTEGER_WRAPPER = 16;

    public final static int SER_NUMBER_LONG = 17;
    public final static int SER_NUMBER_LONG_WRAPPER = 18;

    public final static int SER_NUMBER_FLOAT = 19;
    public final static int SER_NUMBER_FLOAT_WRAPPER = 20;

    public final static int SER_NUMBER_DOUBLE = 21;
    public final static int SER_NUMBER_DOUBLE_WRAPPER = 22;

    public final static int SER_NUMBER_BIG_INTEGER = 23;

    public final static int SER_NUMBER_BIG_DECIMAL = 24;

    // // // Other specific scalar types

    public final static int SER_BOOLEAN = 25;
    public final static int SER_BOOLEAN_WRAPPER = 26;
    public final static int SER_CHAR = 27;

    public final static int SER_ENUM = 28;

    public final static int SER_DATE = 29;
    public final static int SER_CALENDAR = 30;

    public final static int SER_CLASS = 31;
    public final static int SER_FILE = 32;
    public final static int SER_UUID = 33;
    public final static int SER_URL = 34;
    public final static int SER_URI = 35;


    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    public final static int SER_ITERABLE = 36;

    /*
    /**********************************************************************
    /* Other constants
    /**********************************************************************
     */

    protected final static int CACHE_FLAGS = JSON.CACHE_FLAGS;

    /*
    /**********************************************************************
    /* Methods for sub-classes
    /**********************************************************************
     */

    protected int _findSimpleType(Class<?> raw, boolean forSer)
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
            return SER_BOOLEAN_WRAPPER;
        }
        if (Number.class.isAssignableFrom(raw)) {
            if (raw == Integer.class) return SER_NUMBER_INTEGER_WRAPPER;
            if (raw == Long.class) return SER_NUMBER_LONG_WRAPPER;
            if (raw == Double.class) return SER_NUMBER_DOUBLE_WRAPPER;
            if (raw == Float.class) return SER_NUMBER_FLOAT_WRAPPER;
            if (raw == BigDecimal.class) return SER_NUMBER_BIG_DECIMAL;
            if (raw == BigInteger.class) {
                return SER_NUMBER_BIG_INTEGER;
            }
            if (raw == Byte.class) return SER_NUMBER_BYTE;
            if (raw == Short.class) return SER_NUMBER_SHORT;
            // What numeric type is this? Could consider "string-like" but...
            return SER_UNKNOWN;
        }
        if (raw == Character.class) {
            return SER_CHAR;
        }
        if (isEnum(raw)) {
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
        // May or may not help with deser, but recognized nonetheless;
        // on assumption that Beans should rarely implement `CharSequence`
        if (CharSequence.class.isAssignableFrom(raw)) {
            return SER_CHARACTER_SEQUENCE;
        }
        // `Iterable` can be added on all kinds of things, and it won't
        // help at all with deserialization; hence only use for serialization.
        if (forSer && Iterable.class.isAssignableFrom(raw)) {
            // 16-Feb-2024, tatu: [jackson-jr#112] java.nio.file.Path is not really Iterable
            if (!java.nio.file.Path.class.isAssignableFrom(raw)) {
                return SER_ITERABLE;
            }
        }
        
        // Ok. I give up, no idea!
        return SER_UNKNOWN;
    }
}
