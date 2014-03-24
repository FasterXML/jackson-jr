package com.fasterxml.jackson.jr.type;

import java.util.*;

/**
 * Placeholder used for resolving type assignments to figure out
 * type parameters for subtypes.
 */
public class TypePlaceHolder extends ResolvedType
{
    protected final int _ordinal;

    /**
     * Type assigned during wildcard resolution
     */
    protected ResolvedType _actualType;
    
    public TypePlaceHolder(int ordinal)
    {
        super(Object.class, TypeBindings.emptyBindings());
        _ordinal = ordinal;
    }

    public ResolvedType actualType() { return _actualType; }
    public void actualType(ResolvedType t) { _actualType = t; }
    
    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */

    @Override
    public List<ResolvedType> getImplementedInterfaces() { return Collections.<ResolvedType>emptyList(); }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        sb.append('<').append(_ordinal).append('>');
        return sb;
    }
}
