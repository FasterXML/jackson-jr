package com.fasterxml.jackson.simple.ob.impl;

/**
 * Set of types that we want to distinguish, mostly for purposes of
 * knowing how to write them.
 */
public enum SimpleType
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

    // // // String(-like) types

    STRING,
    
    CHARACTER_SEQUENCE,

    // // // Numbers
    
    NUMBER_BYTE,

    NUMBER_SHORT,
    
    NUMBER_INTEGER,

    NUMBER_LONG,

    NUMBER_FLOAT,

    NUMBER_DOUBLE,

    NUMBER_BIG_INTEGER,

    NUMBER_BIG_DECIMAL,

    // // // Other scalar types
    
    BOOLEAN,

    CHAR,
    
    DATE,
    
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
