package com.fasterxml.jackson.jr.type;

import java.util.*;

/**
 * Type implementation for classes that do not represent interfaces,
 * primitive or array types.
 */
public class ResolvedObjectType extends ResolvedType
{
    protected final ResolvedObjectType _superClass;
    /**
     * List of interfaces this type implements; may be empty but never null
     */
    protected final ResolvedType[] _superInterfaces;

    /**
     * Modifiers of the underlying class.
     */
    protected final int _modifiers;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    public ResolvedObjectType(Class<?> erased, TypeBindings bindings,
            ResolvedObjectType superClass, List<ResolvedType> interfaces)
    {
        this(erased, bindings, superClass,
                (interfaces == null || interfaces.isEmpty()) ? NO_TYPES :
                interfaces.toArray(new ResolvedType[interfaces.size()]));
    }

    public ResolvedObjectType(Class<?> erased, TypeBindings bindings,
            ResolvedObjectType superClass, ResolvedType[] interfaces)
    {
        super(erased, bindings);
        _superClass = superClass;
        _superInterfaces = (interfaces == null) ? NO_TYPES : interfaces;
        _modifiers = erased.getModifiers();
    }

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */

    @Override
    public ResolvedObjectType getParentClass() { return _superClass; }

    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return (_superInterfaces.length == 0) ?
                Collections.<ResolvedType>emptyList() : Arrays.asList(_superInterfaces);
    }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        return _appendClassDescription(sb);
    }
}

