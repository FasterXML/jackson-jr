package com.fasterxml.jackson.simple.ob.impl;

/**
 * Set of types that we want to distinguish, mostly for purposes of
 * knowing how to write them.
 */
public enum ValueType
{
    // // // Structured types
    
    /**
     * All kinds of {@link java.util.Map}s.
     */
    MAP,

    /**
     * All kinds of {@link java.util.List}s.
     */
    LIST,

    /**
     * All kinds of {@link java.util.Collection}s other than {@link java.util.List}s
     */
    COLLECTION,

    /**
     * Arrays of non-primitive types
     */
    OBJECT_ARRAY,

    /**
     * An implementation of {@link com.fasterxml.jackson.core.TreeNode}
     */
    TREE_NODE,
    
    // // // String(-like) types

    STRING,
    
    CHARACTER_SEQUENCE,
    
    CHAR_ARRAY,

    BYTE_ARRAY,

    INT_ARRAY,

    // // // Numbers
    
    NUMBER_BYTE,

    NUMBER_SHORT,
    
    NUMBER_INTEGER,

    NUMBER_LONG,

    NUMBER_FLOAT,

    NUMBER_DOUBLE,

    NUMBER_BIG_INTEGER,

    NUMBER_BIG_DECIMAL,

    NUMBER_OTHER,
    
    // // // Other scalar types
    
    BOOLEAN,

    CHAR,

    DATE,

    ENUM,

    // // // Iterate-able types

    /**
     * Anything that implements {@link java.lang.Iterable}, but not
     * {@link java.util.Collection}.
     */
    ITERABLE,
    
    /**
     * Type not otherwise recognized.
     */
    OTHER;
}
