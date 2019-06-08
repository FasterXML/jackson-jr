package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;

abstract class ValueLocatorBase
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
     * Marker for null value
     */
    public final static int SER_NULL = 1;
    
    /**
     * All kinds of {@link java.util.Map}s.
     */
    public final static int SER_MAP = 2;

    /**
     * All kinds of {@link java.util.List}s.
     */
    public final static int SER_LIST = 3;

    /**
     * All kinds of {@link java.util.Collection}s other than {@link java.util.List}s
     */
    public final static int SER_COLLECTION = 4;

    /**
     * Arrays of non-primitive types
     */
    public final static int SER_OBJECT_ARRAY = 5;

    public final static int SER_INT_ARRAY = 6;
    public final static int SER_LONG_ARRAY = 7;
    public final static int SER_BOOLEAN_ARRAY = 8;
    
    /**
     * An implementation of {@link com.fasterxml.jackson.core.TreeNode}
     */
    public final static int SER_TREE_NODE = 9;
    
    // // // String(-like) types

    public final static int SER_STRING = 10;
    public final static int SER_CHARACTER_SEQUENCE = 11;
    public final static int SER_CHAR_ARRAY = 12;
    public final static int SER_BYTE_ARRAY = 13;

    // // // Numbers
    
    public final static int SER_NUMBER_BYTE = 14;

    public final static int SER_NUMBER_SHORT = 15;

    public final static int SER_NUMBER_INTEGER = 16;

    public final static int SER_NUMBER_LONG = 17;

    public final static int SER_NUMBER_FLOAT = 18;

    public final static int SER_NUMBER_DOUBLE = 19;

    public final static int SER_NUMBER_BIG_INTEGER = 20;

    public final static int SER_NUMBER_BIG_DECIMAL = 21;

    // // // Other specific scalar types

    public final static int SER_BOOLEAN = 22;
    public final static int SER_CHAR = 23;

    public final static int SER_ENUM = 24;

    public final static int SER_DATE = 25;
    public final static int SER_CALENDAR = 26;

    public final static int SER_CLASS = 27;
    public final static int SER_FILE = 28;
    public final static int SER_UUID = 29;
    public final static int SER_URL = 30;
    public final static int SER_URI = 31;

    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    public final static int SER_ITERABLE = 32;

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

    protected POJODefinition _resolveBeanDef(Class<?> raw) {
        return POJODefinition.find(raw);
    }

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
    
}
